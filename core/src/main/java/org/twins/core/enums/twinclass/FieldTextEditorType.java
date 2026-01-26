package org.twins.core.enums.twinclass;

public enum FieldTextEditorType {
    PLAIN,
    MARKDOWN_GITHUB,
    MARKDOWN_BASIC,
    HTML;

    @Override
    public String toString() {
        return name();
    }
}
