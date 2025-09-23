package org.twins.core.enums.datalist;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum DataListStatus {
    active("active"),
    disabled("disabled"),
    hidden("hidden");

    private final String id;

    DataListStatus(String id) {
        this.id = id;
    }

    public static DataListStatus valueOd(String type) {
        return Arrays.stream(DataListStatus.values()).filter(t -> t.id.equals(type)).findAny().orElse(active);
    }
}
