package org.twins.core.domain.enum_.factory;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Action {
    NOT_SPECIFIED("NOT_SPECIFIED"),
    RESTRICT("RESTRICT"),
    ERASE_IRREVOCABLE("ERASE_IRREVOCABLE"),
    ERASE_CANDIDATE("ERASE_CANDIDATE");

    private final String id;

    Action(String id) {
        this.id = id;
    }

    public static Action valueOd(String type) {
        return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElse(NOT_SPECIFIED);
    }

}
