package org.twins.core.domain.enum_.twin;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CheckboxType {
    STANDARD("STANDARD"),
    TOGGLE("TOGGLE"),
    CUSTOM("CUSTOM");

    private final String id;

    CheckboxType(String id) {
        this.id = id;
    }

    public static CheckboxType valueOfId(String type) {
        return Arrays.stream(CheckboxType.values()).filter(c -> c.id.equalsIgnoreCase(type)).findAny().orElseThrow();
    }
}
