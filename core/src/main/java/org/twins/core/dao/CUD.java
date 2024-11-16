package org.twins.core.dao;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CUD {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String id;

    CUD(String id) {
        this.id = id;
    }

    public static CUD valueOd(String type) {
        return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElseThrow();
    }
}
