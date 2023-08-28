package org.twins.core.mappers.rest;

import org.cambium.common.exception.ServiceException;

public interface RestDTOMapper<S, D> extends RestDTOConverter<S, D> {
    public void map(S src, D dst) throws ServiceException;
}
