package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.Channel;
import org.twins.core.featurer.tokenhandler.TokenHandler;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
public class UserBusinessAccountResolverAuthToken implements BusinessAccountResolver, UserResolver {
    final HttpRequestService httpRequestService;
    final FeaturerService featurerService;
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
        if (StringUtils.isEmpty(authToken)) //todo delete on production
            authToken = httpRequestService.getBusinessAccountIdFromRequest() + "," + httpRequestService.getUserIdFromRequest();
        DomainEntity domainEntity = authService.getApiUser().getDomain(); // warning recursion call risk
        TokenHandler tokenHandler = featurerService.getFeaturer(domainEntity.getTokenHandlerFeaturer(), TokenHandler.class);
        TokenHandler.Result result = tokenHandler.resolveUserIdAndBusinessAccountId(domainEntity.getTokenHandlerParams(), authToken, domainEntity, Channel.WEB);
        userId = result.getUserId();
        businessAccountId = result.getBusinessAccountId();
        resolved = true;
    }
}
