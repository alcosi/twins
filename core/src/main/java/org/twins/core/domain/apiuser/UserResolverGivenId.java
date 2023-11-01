package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.UUID;

public class UserResolverGivenId implements UserResolver {
    private final UUID userId;

    public UserResolverGivenId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public UUID resolveCurrentUserId() throws ServiceException {
        return userId;
    }
}
