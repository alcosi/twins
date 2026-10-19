package org.twins.core.mappers.rest;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

public abstract class RestSimpleDTOConverter<T, S> extends RestSimpleDTOMapper<T, S> {
    @Override
    public void map(T src, S dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public abstract S convert(T src, MapperContext mapperContext) throws Exception;
}
