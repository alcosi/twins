package org.twins.core.mappers.rest;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class RestSimpleDTOMapper<T, S> extends RestListDTOMapper<T, S> {
    private final Class<S> type;

    public RestSimpleDTOMapper() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        type = (Class) pt.getActualTypeArguments()[1];
    }

    public S convert(T src) throws Exception {
        return convert(src, new MapperContext());
    }

    public S convert(T src, MapperContext mapperContext) throws Exception {
        if (hideMode(mapperContext))
            return null;
        S dst = type.getDeclaredConstructor().newInstance();
        map(src, dst, mapperContext);
        return dst;
    }

    public S convertOrPostpone(T src, MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations())
            return convert(src, mapperContext);
        else {
            mapperContext.addRelatedObject(src);
            return null;
        }
    }

    public boolean hideMode(MapperContext mapperContext) {
        return false;
    }
}
