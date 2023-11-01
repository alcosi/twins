package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.HttpRequestService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserResolverHeaders implements UserResolver {
    final HttpRequestService httpRequestService;
    @Override
    public UUID resolveCurrentUserId() throws ServiceException {
        UUID userId;
        try {
            userId = UUID.fromString(httpRequestService.getUserIdFromRequest());
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect " + HttpRequestService.HEADER_USER_ID + " header");
        }
        return userId;
    }
}
