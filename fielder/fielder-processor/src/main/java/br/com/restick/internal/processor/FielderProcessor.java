package br.com.restick.internal.processor;

import br.com.restick.api.annotation.Fielder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

import br.com.restick.internal.massager.FieldsMessager;
import br.com.restick.internal.validator.FieldsValidator;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import static br.com.restick.internal.processor.unwrapper.UnwrapperIntelliJ.unwrapIntelliJ;

/**
 * Annotation Processor que processa anotações {@link br.com.restick.api.annotation.Fielder}.
 * <p>
 * Esta classe utiliza a API interna do Javac (JavacTrees, TreeMaker, Names) para
 * modificar a AST durante a compilação, adicionando campos públicos dinamicamente
 * às classes anotadas.
 * </p>
 * <p>
 * Detalhes importantes:
 * <ul>
 *   <li>Os modificadores de campos são definidos usando valores {@code long} do TreeMaker:
 *       0L = nenhum, 1L = public, 2L = private, 4L = protected, 8L = static, 16L = final.</li>
 *   <li>O tipo do campo é passado como String na anotação e convertido para {@link JCTree.JCIdent}.</li>
 *   <li>Campos gerados em tempo de compilação exigem que o módulo consumidor compile
 *       após o processor gerar os campos, caso contrário o bytecode não terá o campo público.</li>
 * </ul>
 * </p>
 *
 * @author William
 * @since 1.0
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("br.com.restick.api.annotation.Fielder")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class FielderProcessor extends AbstractProcessor {

    /** Instância do JavacTrees para manipulação da AST */
    private JavacTrees trees;

    /** Factory para criação de elementos da AST */
    private TreeMaker maker;

    /** Helper para criação de nomes no AST */
    private Names names;

    /** Validator para garantir o uso correto das anotações */
    private FieldsValidator validator;

    /** Messager para log de infos, warnings e errors */
    private FieldsMessager messager;

    /**
     * Inicializa o processor, configurando as instâncias de {@link JavacTrees}, {@link TreeMaker} e {@link Names}.
     *
     * @param processingEnv o ambiente de processamento fornecido pelo compilador
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        // Sempre trabalhar com o ProcessingEnvironment real, se houver wrapper
        ProcessingEnvironment env = unwrapIntelliJ(processingEnv);

        // Validação explícita: este processor depende do javac
        if (!(env instanceof com.sun.tools.javac.processing.JavacProcessingEnvironment javacEnv)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "AddFieldProcessor requires javac (com.sun.tools.javac). " +
                            "This compiler is not supported."
            );
            return;
        }

        trees = JavacTrees.instance(env);

        Context context = javacEnv.getContext();
        maker = TreeMaker.instance(context);
        names = Names.instance(context);

        var prcsEnvMessager = processingEnv.getMessager();
        var fieldMessager = new FieldsMessager(prcsEnvMessager);
        validator = new FieldsValidator(fieldMessager);
        messager = fieldMessager;
    }

    /**
     * Processa todos os elementos anotados com {@link Fielder}.
     * <p>
     * Para cada classe anotada, cria um novo campo público com o nome e tipo especificados
     * na anotação e adiciona diretamente na AST da classe.
     * </p>
     *
     * @param annotations conjunto de anotações encontradas nesta rodada de processamento
     * @param roundEnv ambiente que fornece acesso aos elementos anotados
     * @return {@code true} se o processamento desta anotação foi concluído com sucesso
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Fielder.class)) {
            if (!(element instanceof TypeElement type)) continue;

            Fielder annotation = element.getAnnotation(Fielder.class);

            if(!validator.isEligible(type, annotation.name())){
                continue;
            }

            JCTree.JCClassDecl classDecl = trees.getTree(type);

            JCTree.JCVariableDecl field = getJcVariableDecl(annotation);

            // Adiciona o campo na classe
            messager.note(type, "@Fielder: Incluindo campo(s) na classe - " + type.getSimpleName());
            classDecl.defs = classDecl.defs.prepend(field);
        }
        return true;
    }

    private JCTree.JCVariableDecl getJcVariableDecl(Fielder annotation) {
        TypeMirror typeMirror = null;

        try{
            annotation.type();
            throw new IllegalStateException("Nunca deveria acessar Class diretamente");
        } catch (MirroredTypeException e){
            typeMirror = e.getTypeMirror();
        }

        TypeElement typeElement =
                (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

        String simpleTypeName = typeElement.getSimpleName().toString();

        return maker.VarDef(
                maker.Modifiers(1L),
                names.fromString(annotation.name()),
                maker.Ident(names.fromString(simpleTypeName)), // tipo
                null // inicialização
        );
    }
}
