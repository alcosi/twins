package org.twins.core.mappers.rest;

public interface RestDTOMapper<S, D> extends RestDTOConverter<S, D> {
    void map(S src, D dst, MapperProperties mapperProperties) throws Exception;
}
