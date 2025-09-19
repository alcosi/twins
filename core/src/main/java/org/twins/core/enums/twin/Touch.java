package org.twins.core.enums.twin;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Touch {
    WATCHED("WATCHED"),
    STARRED("STARRED"),
    REVIEWED("REVIEWED");

    private final String id;

    Touch(String id) {
        this.id = id;
    }

    public static Touch valueOfId(String type) {
        return Arrays.stream(Touch.values()).filter(t -> t.id.equalsIgnoreCase(type)).findAny().orElseThrow();
    }
}
