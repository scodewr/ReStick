package br.com.restick.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.SOURCE)
@Target(TYPE)
public @interface AddFields {

    /**
     * Lista de definições de campos a serem gerados na classe anotada.
     */
    FieldDef[] value();
}
