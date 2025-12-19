package br.com.restick.internal.validator;

import br.com.restick.internal.massager.FieldsMessager;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Classe responsável por validar se um {@link TypeElement} é elegível para
 * receber campos gerados em tempo de compilação.
 * <p>
 * Esta validação é executada antes da modificação da AST, garantindo que a
 * classe alvo atenda aos pré-requisitos estruturais esperados pelo processor.
 * </p>
 *
 * <p>
 * As validações atuais incluem:
 * <ul>
 *   <li>O elemento deve ser uma classe concreta.</li>
 *   <li>A classe deve possuir um construtor vazio (em qualquer visibilidade).</li>
 *   <li>O campo a ser gerado não deve já existir na classe.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Caso o campo já exista, um {@code warning} é emitido e o processamento
 * continua sem gerar o campo novamente.
 * </p>
 *
 * @author William
 * @since 1.0
 */
@RequiredArgsConstructor
public class FieldsValidator {

    private final FieldsMessager messager;

    /**
     * Verifica se a classe informada é elegível para receber um novo campo.
     *
     * @param type o {@link TypeElement} representando a classe anotada
     * @param fieldName o nome do campo que será gerado
     * @return {@code true} se a classe for elegível; {@code false} caso contrário
     */
    public boolean isEligible(TypeElement type, String fieldName) {

        // Validação: apenas classes são suportadas
        if (type.getKind() != ElementKind.CLASS) {
            messager.warning(
                    type,
                    "@Fielder: uso permitido apenas em classes. Sem efeito sob outros TypeElement."
            );
            return false;
        }

        boolean hasNoArgsConstructor = false;

        // Varre os membros da classe
        for (Element enclosed : type.getEnclosedElements()) {

            // Verifica construtor vazio
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ctor = (ExecutableElement) enclosed;
                if (ctor.getParameters().isEmpty()) {
                    hasNoArgsConstructor = true;
                }
            }

            // Verifica se o campo já existe
            if (enclosed.getKind() == ElementKind.FIELD
                    && enclosed.getSimpleName().contentEquals(fieldName)) {

                messager.warning(
                        enclosed,
                        "@Fielder: campo '%s' foi previamente criado na classe. A Annotation nao produzirá novo campo.",
                        fieldName
                );

                // Não é erro fatal, apenas impede geração duplicada
                return false;
            }
        }

        // Validação: construtor vazio obrigatório
        if (!hasNoArgsConstructor) {
            messager.error(
                    type,
                    "@Fielder: a classe deve possuir construtor vazio "
                            + "(private, protected ou public)"
            );
            return false;
        }

        return true;
    }
}
