package org.twins.core.enums.twinclass;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum FieldCheckboxType {
    STANDARD("STANDARD"),
    TOGGLE("TOGGLE"),
    CUSTOM("CUSTOM");

    private final String id;

    FieldCheckboxType(String id) {
        this.id = id;
    }

    public static FieldCheckboxType valueOfId(String type) {
        return Arrays.stream(FieldCheckboxType.values()).filter(c -> c.id.equalsIgnoreCase(type)).findAny().orElseThrow();
    }
}
