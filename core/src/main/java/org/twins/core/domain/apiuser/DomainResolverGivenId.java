package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.UUID;

public class DomainResolverGivenId implements DomainResolver {
    private final UUID domainId;

    public DomainResolverGivenId(UUID domainId) {
        this.domainId = domainId;
    }

    @Override
    public UUID resolveCurrentDomainId() throws ServiceException {
        return domainId;
    }
}
