package org.twins.core.mappers.rest;

import org.twins.core.mappers.rest.mappercontext.MapperContext;

public interface RestDTOMapper<S, D> extends RestDTOConverter<S, D> {
    void map(S src, D dst, MapperContext mapperContext) throws Exception;
}
