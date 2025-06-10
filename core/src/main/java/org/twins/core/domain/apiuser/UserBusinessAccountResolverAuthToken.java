package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.IdentityProviderService;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
@Slf4j
public class UserBusinessAccountResolverAuthToken implements BusinessAccountResolver, UserResolver {
    final HttpRequestService httpRequestService;
    final IdentityProviderService identityProviderService;
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
        log.info("Auth token: {}", authToken);
        TokenMetaData result = identityProviderService.resolveAuthTokenMetaData(authToken);
        userId = result.getUserId();
        businessAccountId = result.getBusinessAccountId();
        resolved = true;
        String actAsUserHeader = httpRequestService.getActAsUserFromRequest();
        if (StringUtils.isEmpty(actAsUserHeader)) {
            return;
        }
        ActAsUser actAsUser = identityProviderService.resolveActAsUser(actAsUserHeader);
        //todo check permission to act as user
        userId = actAsUser.getUserId();
        businessAccountId = actAsUser.getBusinessAccountId();
    }
}
