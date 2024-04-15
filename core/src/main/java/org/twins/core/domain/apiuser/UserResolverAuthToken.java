package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.service.HttpRequestService;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
public class UserResolverAuthToken implements UserResolver {
    final HttpRequestService httpRequestService;
    @Override
    public UUID resolveCurrentUserId() throws ServiceException {
        String authToken = httpRequestService.getAuthTokenFromRequest();
        //todo fixme with crypt
        String[] tokenData = authToken.split(",");
        return UUID.fromString(tokenData[0].trim());
    }
}
