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
        S dst = type.getDeclaredConstructor().newInstance();
        map(src, dst, new MapperProperties());
        return dst;
    }

    public S convert(T src, MapperProperties mapperProperties) throws Exception {
        S dst = type.getDeclaredConstructor().newInstance();
        map(src, dst, mapperProperties);
        return dst;
    }
}
