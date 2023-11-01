package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.UUID;

public class BusinessAccountResolverGivenId implements BusinessAccountResolver {
    private final UUID businessAccountId;

    public BusinessAccountResolverGivenId(UUID businessAccountId) {
        this.businessAccountId = businessAccountId;
    }

    @Override
    public UUID resolveCurrentBusinessAccountId() throws ServiceException {
        return businessAccountId;
    }
}
