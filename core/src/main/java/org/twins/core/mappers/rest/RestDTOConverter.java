package org.twins.core.mappers.rest;

import org.cambium.common.exception.ServiceException;

public interface RestDTOConverter<S, D> {
    public D convert(S src) throws ServiceException;
}
