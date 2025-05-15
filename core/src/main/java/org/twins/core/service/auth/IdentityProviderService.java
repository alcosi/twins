package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.connector.IdentityProviderConnector;
import org.twins.core.featurer.identityprovider.token.ClientTokenData;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class IdentityProviderService {
    private final AuthService authService;
    private final FeaturerService featurerService;

    public ClientTokenData login(String username, String password) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        IdentityProviderEntity identityProvider = apiUser.getDomain().getIdentityProvider();
        if (identityProvider.getStatus() != IdentityProviderEntity.IdentityProviderStatus.ACTIVE) {
            throw new ServiceException(ErrorCodeTwins.IDP_IS_NOT_ACTIVE);
        }
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturer(), IdentityProviderConnector.class);
        return identityProviderConnector.login(identityProvider.getIdentityProviderConnectorParams(), username, password);
    }

    public void logout() {

    }
}
