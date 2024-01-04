package org.twins.core.mappers.rest;

import lombok.Getter;

public class RelatedObject <T> {
    @Getter
    private final T object;

    @Getter
    private final MapperModeMap modes;

    public RelatedObject(T object, MapperModeMap modes) {
        this.object = object;
        this.modes = modes;
    }
}
