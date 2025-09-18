package org.twins.core.domain.enum_.featurer.fieldtyper;

public enum TextEditorType {
    PLAIN,
    MARKDOWN_GITHUB,
    MARKDOWN_BASIC,
    HTML;

    @Override
    public String toString() {
        return name();
    }
}
