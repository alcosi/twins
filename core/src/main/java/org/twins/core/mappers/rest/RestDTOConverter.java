package org.twins.core.mappers.rest;


public interface RestDTOConverter<S, D> {
    public D convert(S src, MapperContext mapperContext) throws Exception;
}
