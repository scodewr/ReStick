package br.com.restick.internal.massager;

import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

@RequiredArgsConstructor
public class FieldsMessager {

    private final Messager messager;

    /* =========================
       ERROS
       ========================= */

    public void error(Element element, String message) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                message,
                element
        );
    }

    public void error(Element element, String format, Object... args) {
        error(element, String.format(format, args));
    }

    /* =========================
       WARNINGS
       ========================= */

    public void warning(Element element, String message) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                message,
                element
        );
    }

    public void warning(Element element, String format, Object... args) {
        warning(element, String.format(format, args));
    }

    /* =========================
       INFO / NOTE
       ========================= */

    public void note(Element element, String message) {
        messager.printMessage(
                Diagnostic.Kind.NOTE,
                message,
                element
        );
    }

    public void note(Element element, String format, Object... args) {
        note(element, String.format(format, args));
    }
}
