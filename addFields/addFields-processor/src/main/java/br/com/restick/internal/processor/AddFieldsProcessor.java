package br.com.restick.internal.processor;

import br.com.restick.api.annotation.AddFields;
import br.com.restick.api.annotation.FieldDef;
import br.com.restick.internal.massager.FieldsMessager;
import br.com.restick.internal.validator.FieldsValidator;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

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

import static br.com.restick.internal.processor.unwrapper.UnwrapperIntelliJ.unwrapIntelliJ;

/**
 * Annotation Processor que processa {@link AddFields}.
 * <p>
 * Permite a geração de múltiplos campos públicos em uma classe, definidos
 * por meio de uma lista de {@link FieldDef}.
 * </p>
 *
 * <p>
 * Este processor:
 * <ul>
 *   <li>Opera diretamente sobre a AST usando APIs internas do Javac</li>
 *   <li>Suporta múltiplos campos por classe</li>
 *   <li>Evita acesso direto a {@code Class<?>} usando {@link MirroredTypeException}</li>
 *   <li>Garante compatibilidade com IntelliJ via unwrap do ProcessingEnvironment</li>
 * </ul>
 * </p>
 *
 * @author William
 * @since 1.0
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("br.com.restick.api.annotation.AddFields")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class AddFieldsProcessor extends AbstractProcessor {

    /** Instância do JavacTrees para manipulação da AST */
    private JavacTrees trees;

    /** Factory para criação de nós da AST */
    private TreeMaker maker;

    /** Helper para criação de identificadores */
    private Names names;

    /** Validador de regras de uso */
    private FieldsValidator validator;

    /** Messager customizado */
    private FieldsMessager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        ProcessingEnvironment env = unwrapIntelliJ(processingEnv);

        if (!(env instanceof JavacProcessingEnvironment javacEnv)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "AddFieldsProcessor requires javac (com.sun.tools.javac). " +
                            "This compiler is not supported."
            );
            return;
        }

        trees = JavacTrees.instance(env);

        Context context = javacEnv.getContext();
        maker = TreeMaker.instance(context);
        names = Names.instance(context);

        var procMessager = processingEnv.getMessager();
        messager = new FieldsMessager(procMessager);
        validator = new FieldsValidator(messager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(AddFields.class)) {
            if (!(element instanceof TypeElement type)) continue;

            AddFields annotation = element.getAnnotation(AddFields.class);

            JCTree.JCClassDecl classDecl = trees.getTree(type);

            for (FieldDef fieldDef : annotation.value()) {

                if (!validator.isEligible(type, fieldDef.name())) {
                    continue;
                }

                JCTree.JCVariableDecl fieldDecl = createField(fieldDef);

                messager.note(
                        type,
                        "@AddFields: Incluindo campo '" + fieldDef.name()
                                + "' na classe " + type.getSimpleName()
                );

                classDecl.defs = classDecl.defs.prepend(fieldDecl);
            }
        }

        return true;
    }

    /**
     * Cria um campo {@link JCTree.JCVariableDecl} a partir de um {@link FieldDef}.
     */
    private JCTree.JCVariableDecl createField(FieldDef fieldDef) {

        TypeMirror typeMirror;

        try {
            fieldDef.type();
            throw new IllegalStateException("Nunca deveria acessar Class diretamente");
        } catch (MirroredTypeException e) {
            typeMirror = e.getTypeMirror();
        }

        TypeElement typeElement =
                (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);

        String simpleTypeName = typeElement.getSimpleName().toString();

        return maker.VarDef(
                maker.Modifiers(fieldDef.modifier()),
                names.fromString(fieldDef.name()),
                maker.Ident(names.fromString(simpleTypeName)),
                null
        );
    }
}
