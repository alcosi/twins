package org.twins.core.mappers.rest.mappercontext;

import java.util.Hashtable;

public class MapperModeMap extends Hashtable<Class<MapperMode>, MapperMode> {

    public MapperModeMap() {
        super();
    }

    public MapperModeMap(MapperMode... mapperModes) {
        super();
        if (mapperModes != null)
            for (MapperMode mapperMode : mapperModes) {
                put(mapperMode);
            }
    }

    public MapperModeMap(MapperModeMap mapperModeMap) {
        super(mapperModeMap);
    }

    public MapperModeMap put(MapperMode mapperMode) {
        put((Class<MapperMode>) mapperMode.getClass(), mapperMode);
        return this;
    }

    public synchronized MapperMode remove(MapperMode key) {
        return super.remove(key.getClass());
    }
}
