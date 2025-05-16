package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientTokenData;
import org.twins.core.featurer.identityprovider.connector.IdentityProviderConnector;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class IdentityProviderService extends TwinsEntitySecureFindService<IdentityProviderEntity> {
    private final AuthService authService;
    private final FeaturerService featurerService;
    private final IdentityProviderRepository identityProviderRepository;

    @Override
    public CrudRepository<IdentityProviderEntity, UUID> entityRepository() {
        return identityProviderRepository;
    }

    @Override
    public Function<IdentityProviderEntity, UUID> entityGetIdFunction() {
        return IdentityProviderEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(IdentityProviderEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(IdentityProviderEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @NotNull
    private IdentityProviderEntity getDomainIdentityProviderSafe() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        IdentityProviderEntity identityProvider = apiUser.getDomain().getIdentityProvider();
        if (identityProvider.getStatus() != IdentityProviderEntity.IdentityProviderStatus.ACTIVE) {
            throw new ServiceException(ErrorCodeTwins.IDP_IS_NOT_ACTIVE);
        }
        return identityProvider;
    }

    public ClientTokenData login(String username, String password) throws ServiceException {
        return login(username, password, null);
    }

    public ClientTokenData login(String username, String password, String fingerprint) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturer(), IdentityProviderConnector.class);
        return identityProviderConnector.login(identityProvider.getIdentityProviderConnectorParams(), username, password, fingerprint);
    }

    public void logout() throws ServiceException{
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturer(), IdentityProviderConnector.class);
        return identityProviderConnector.logout(identityProvider.getIdentityProviderConnectorParams(), username, password);
    }

    public ClientTokenData refresh(String refreshToken) throws ServiceException {
        return refresh(refreshToken, null);
    }

    public ClientTokenData refresh(String refreshToken, String fingerprint) throws ServiceException {
        IdentityProviderEntity identityProvider = getDomainIdentityProviderSafe();
        IdentityProviderConnector identityProviderConnector = featurerService.getFeaturer(identityProvider.getIdentityProviderConnectorFeaturer(), IdentityProviderConnector.class);
        return identityProviderConnector.refresh(identityProvider.getIdentityProviderConnectorParams(), refreshToken, fingerprint);
    }
}
