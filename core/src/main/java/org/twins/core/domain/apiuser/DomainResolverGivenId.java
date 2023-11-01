package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.HttpRequestService;

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
