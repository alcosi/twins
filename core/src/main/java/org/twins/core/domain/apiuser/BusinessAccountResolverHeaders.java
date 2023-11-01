package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.HttpRequestService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BusinessAccountResolverHeaders implements BusinessAccountResolver {
    final HttpRequestService httpRequestService;
    @Override
    public UUID resolveCurrentBusinessAccountId() throws ServiceException {
        UUID businessAccountId;
        try {
            businessAccountId = UUID.fromString(httpRequestService.getBusinessAccountIdFromRequest());
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + HttpRequestService.HEADER_BUSINESS_ACCOUNT_ID + " header");
        }
        return businessAccountId;
    }
}
