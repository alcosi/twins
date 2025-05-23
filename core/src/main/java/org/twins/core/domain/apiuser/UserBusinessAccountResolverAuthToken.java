package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.auth.IdentityProviderService;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
public class UserBusinessAccountResolverAuthToken implements BusinessAccountResolver, UserResolver {
    final HttpRequestService httpRequestService;
    final IdentityProviderService identityProviderService;
    @Lazy
    final AuthService authService;
    private UUID userId;
    private UUID businessAccountId;
    private boolean resolved = false;
    @Override
    public UUID resolveCurrentBusinessAccountId() throws ServiceException {
        resolve();
        return businessAccountId;
    }

    @Override
    public UUID resolveCurrentUserId() throws ServiceException {
        resolve();
        return userId;
    }

    public void resolve() throws ServiceException {
        if (resolved)
            return;
        String authToken = httpRequestService.getAuthTokenFromRequest();
        TokenMetaData result = identityProviderService.resolveAuthTokenMetaData(authToken);
        userId = result.getUserId();
        businessAccountId = result.getBusinessAccountId();
        resolved = true;
    }
}
