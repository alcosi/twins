package org.twins.core.mappers.rest;

import org.springframework.data.domain.Pageable;

import java.util.Hashtable;

public class MapperModePaginationMap extends Hashtable<Class<MapperMode>, Pageable> {

    public MapperModePaginationMap() {
        super();
    }

    public MapperModePaginationMap(MapperModePaginationMap mapperModeMap) {
        super(mapperModeMap);
    }

    public MapperModePaginationMap put(MapperMode mapperMode, Pageable pageable) {
        put((Class<MapperMode>) mapperMode.getClass(), pageable);
        return this;
    }
}
