package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.HttpRequestService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DomainResolverHeaders implements DomainResolver {
    final HttpRequestService httpRequestService;

    @Override
    public UUID resolveCurrentDomainId() throws ServiceException {
        UUID domainId;
        try {
            domainId = UUID.fromString(httpRequestService.getDomainIdFromRequest());
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + HttpRequestService.HEADER_DOMAIN_ID + " header");
        }
        return domainId;
    }
}
