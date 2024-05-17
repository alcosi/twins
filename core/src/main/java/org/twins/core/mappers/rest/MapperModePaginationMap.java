package org.twins.core.mappers.rest;

import org.twins.core.service.pagination.SimplePagination;

import java.util.Hashtable;

public class MapperModePaginationMap extends Hashtable<Class<MapperMode>, SimplePagination> {

    public MapperModePaginationMap() {
        super();
    }

    public MapperModePaginationMap(MapperModePaginationMap mapperModeMap) {
        super(mapperModeMap);
    }

    public MapperModePaginationMap put(MapperMode mapperMode, SimplePagination pagination) {
        put((Class<MapperMode>) mapperMode.getClass(), pagination);
        return this;
    }
}
