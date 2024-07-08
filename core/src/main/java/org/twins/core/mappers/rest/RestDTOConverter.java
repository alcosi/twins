package org.twins.core.mappers.rest;


import org.twins.core.mappers.rest.mappercontext.MapperContext;

public interface RestDTOConverter<S, D> {
    public D convert(S src, MapperContext mapperContext) throws Exception;
}
