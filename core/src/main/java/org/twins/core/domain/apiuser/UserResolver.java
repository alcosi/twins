package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.UUID;

public interface UserResolver {
    UUID resolveCurrentUserId() throws ServiceException;
}
