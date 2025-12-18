package br.com.restick.processor;

import br.com.restick.api.annotation.Fielder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

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
@SupportedAnnotationTypes("br.com.restick.api.annotation.Fielder")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class FielderProcessor extends AbstractProcessor {

    /** Instância do JavacTrees para manipulação da AST */
    private JavacTrees trees;

    /** Factory para criação de elementos da AST */
    private TreeMaker maker;

    /** Helper para criação de nomes no AST */
    private Names names;

    /**
     * Inicializa o processor, configurando as instâncias de {@link JavacTrees}, {@link TreeMaker} e {@link Names}.
     *
     * @param processingEnv o ambiente de processamento fornecido pelo compilador
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = JavacTrees.instance(processingEnv);
        Context context = ((com.sun.tools.javac.processing.JavacProcessingEnvironment) processingEnv)
                .getContext();
        maker = TreeMaker.instance(context);
        names = Names.instance(context);
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

            JCTree.JCClassDecl classDecl = trees.getTree(type);

            Fielder annotation = element.getAnnotation(Fielder.class);

            // Cria o campo: public <tipo> <nome>;
            // Modifiers do TreeMaker usam valores long específicos:
            // 0L = nenhum, 1L = public, 2L = private, 4L = protected, 8L = static, 16L = final, etc.
            // Para múltiplos modifiers, use soma ou bitwise OR, ex: 1L | 8L = public static
            JCTree.JCVariableDecl field = maker.VarDef(
                    maker.Modifiers(Modifier.PUBLIC.ordinal() + 1),
                    names.fromString(annotation.name()),
                    maker.Ident(names.fromString(annotation.type())), // tipo
                    null // inicialização
            );

            // Adiciona o campo na classe
            classDecl.defs = classDecl.defs.prepend(field);
        }
        return true;
    }
}
