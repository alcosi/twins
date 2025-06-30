package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.auth.IdentityProviderService;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
public class MainResolverAuthToken implements BusinessAccountResolver, UserResolver, MachineUserResolver, MachineBusinessAccountResolver {
    final HttpRequestService httpRequestService;
    final IdentityProviderService identityProviderService;
    private UUID userId;
    private UUID machineUserId;
    private UUID machineBusinessAccountId;
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

    @Override
    public UUID resolveCurrentMachineUserId() throws ServiceException {
        if (machineUserId != null)
            return machineUserId;
        if (hasActAsUserHeader()) {
            resolve();
        } else {
            machineUserId = ApiUser.NOT_SPECIFIED;
        }
        return machineUserId;
    }

    @Override
    public UUID resolveMachineBusinessAccountId() throws ServiceException {
        if (machineBusinessAccountId != null)
            return machineBusinessAccountId;
        if (hasActAsUserHeader()) {
            resolve();
        } else {
            machineBusinessAccountId = ApiUser.NOT_SPECIFIED;
        }
        return machineBusinessAccountId;
    }

    public void resolve() throws ServiceException {
        if (resolved)
            return;
        String authToken = httpRequestService.getAuthTokenFromRequest();
        TokenMetaData result = identityProviderService.resolveAuthTokenMetaData(authToken);
        String actAsUserHeader = httpRequestService.getActAsUserFromRequest();
        if (StringUtils.isEmpty(actAsUserHeader)) {
            userId = result.getUserId();
            businessAccountId = result.getBusinessAccountId();
        } else {
            ActAsUser actAsUser = identityProviderService.resolveActAsUser(actAsUserHeader);
            //permission to act as user will be checked later
            machineUserId = result.getUserId();
            machineBusinessAccountId = result.getBusinessAccountId();
            userId = actAsUser.getUserId();
            businessAccountId = actAsUser.getBusinessAccountId();
        }
        resolved = true;
    }

    private boolean hasActAsUserHeader() {
        return StringUtils.isNotEmpty(httpRequestService.getActAsUserFromRequest());
    }
}
