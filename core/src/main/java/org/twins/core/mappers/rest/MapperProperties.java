package org.twins.core.mappers.rest;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Hashtable;

@Data
@Accessors(fluent = true)
public class MapperProperties {
    private Hashtable<Class<MapperMode>, MapperMode> modes = new Hashtable<>();

    public static MapperProperties create() {
        return new MapperProperties();
    }

    public MapperProperties setMode(MapperMode mapperMode) {
        modes.put((Class<MapperMode>) mapperMode.getClass(), mapperMode);
        return this;
    }

    public <T extends MapperMode> T getModeOrUse(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode != null)
            return (T) configuredMode;
        else
            return mode;
    }
}
