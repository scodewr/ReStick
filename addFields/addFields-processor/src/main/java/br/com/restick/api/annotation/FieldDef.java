package br.com.restick.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

@Retention(RetentionPolicy.SOURCE)
@Target(ANNOTATION_TYPE)
public @interface FieldDef {

    /**
     * Nome do campo a ser gerado.
     */
    String name();

    /**
     * Tipo do campo (ex: "String", "Long", "java.time.Instant").
     */
    Class<?> type();

    /** Tipo de modificador de acesso e visibilidade do campo (ex: 1L = Public).*/
    long modifier();
}
