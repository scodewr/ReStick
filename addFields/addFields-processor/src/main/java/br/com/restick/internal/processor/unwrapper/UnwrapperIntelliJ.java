package br.com.restick.internal.processor.unwrapper;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.Method;

/**
 * Utilitário responsável por remover ("unwrap") o {@link ProcessingEnvironment}
 * quando ele está envolvido por wrappers do IntelliJ IDEA.
 * <p>
 * Durante a compilação dentro do IntelliJ, o Annotation Processor não recebe
 * diretamente uma instância de {@code JavacProcessingEnvironment}. Em vez disso,
 * o ambiente é encapsulado por wrappers internos do JPS (Java Project System),
 * o que impede o acesso direto às APIs internas do {@code javac}, como
 * {@code JavacTrees}, {@code TreeMaker} e {@code Context}.
 * </p>
 *
 * <p>
 * Esta classe utiliza reflexão para tentar localizar e invocar o método
 * {@code org.jetbrains.jps.javac.APIWrappers.unwrap}, que é o mesmo mecanismo
 * utilizado internamente por bibliotecas como o Lombok para recuperar o
 * {@link ProcessingEnvironment} real.
 * </p>
 *
 * <p>
 * Caso o código não esteja sendo executado dentro do IntelliJ, ou se o wrapper
 * não estiver presente, o método simplesmente retorna o {@code processingEnv}
 * original, garantindo compatibilidade com Maven, Gradle CLI e outras IDEs
 * baseadas diretamente no {@code javac}.
 * </p>
 *
 * <p><b>Observação importante:</b></p>
 * <ul>
 *   <li>Esta classe não fornece suporte para o Eclipse/ECJ, pois o Eclipse não
 *       utiliza o {@code javac} como compilador.</li>
 *   <li>O uso desta classe é indicado apenas para Annotation Processors que
 *       dependem de {@code com.sun.tools.javac.*}.</li>
 * </ul>
 *
 * @author William
 * @since 1.0
 */
public class UnwrapperIntelliJ {

    /**
     * Tenta remover o wrapper do IntelliJ IDEA do {@link ProcessingEnvironment}.
     * <p>
     * Se o ambiente de processamento estiver sendo executado dentro do IntelliJ,
     * este método retorna a instância real do {@link ProcessingEnvironment}
     * utilizada pelo {@code javac}. Caso contrário, retorna o próprio objeto
     * recebido como parâmetro.
     * </p>
     *
     * @param processingEnv o ambiente de processamento fornecido pelo compilador
     * @return o {@link ProcessingEnvironment} real (sem wrapper), ou o próprio
     *         {@code processingEnv} se nenhum wrapper for encontrado
     */
    public static ProcessingEnvironment unwrapIntelliJ(ProcessingEnvironment processingEnv) {
        try {
            Class<?> apiWrappers = processingEnv.getClass()
                    .getClassLoader()
                    .loadClass("org.jetbrains.jps.javac.APIWrappers");

            Method unwrapMethod = apiWrappers.getDeclaredMethod(
                    "unwrap", Class.class, Object.class
            );

            Object unwrapped = unwrapMethod.invoke(
                    null, ProcessingEnvironment.class, processingEnv
            );

            if (unwrapped instanceof ProcessingEnvironment env) {
                return env;
            }
        } catch (Throwable ignored) {
            // Não é IntelliJ ou wrapper não presente
        }

        return processingEnv;
    }
}
