package org.twins.core.domain.enum_.datalist;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Status {
    active("active"),
    disabled("disabled"),
    hidden("hidden");

    private final String id;

    Status(String id) {
        this.id = id;
    }

    public static Status valueOd(String type) {
        return Arrays.stream(Status.values()).filter(t -> t.id.equals(type)).findAny().orElse(active);
    }
}
