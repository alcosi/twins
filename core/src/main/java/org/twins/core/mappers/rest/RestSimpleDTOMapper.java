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
        return convert(src, new MapperProperties());
    }

    public S convert(T src, MapperProperties mapperProperties) throws Exception {
        if (hideMode(mapperProperties))
            return null;
        S dst = type.getDeclaredConstructor().newInstance();
        map(src, dst, mapperProperties);
        return dst;
    }

    public S convertOrPostpone(T src, MapperProperties mapperProperties) throws Exception {
        if (mapperProperties.isLazyRelations())
            return convert(src, mapperProperties);
        else {
            mapperProperties.addRelatedObject(src);
            return null;
        }
    }

    public boolean hideMode(MapperProperties mapperProperties) {
        return false;
    }
}
