package org.twins.core.mappers.rest;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Hashtable;

@Data
@Accessors(fluent = true)
public class MapperProperties {
    private Hashtable<String, Object> properties = new Hashtable<>();
    private Hashtable<Class<MapperMode>, MapperMode> modes = new Hashtable<>();

    public static MapperProperties create() {
        return new MapperProperties();
    }

    public MapperProperties setMode(MapperMode mapperMode) {
        modes.put((Class<MapperMode>) mapperMode.getClass(), mapperMode);
        return this;
    }

    public MapperProperties addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public <T> T getProperty(String key, Class<T> type) {
        return (T) properties.get(key);
    }

    public MapperProperties setModeIfNotPresent(MapperMode mapperMode) {
        MapperMode configuredMode = modes.get(mapperMode.getClass());
        if (configuredMode == null)
            setMode(mapperMode);
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
