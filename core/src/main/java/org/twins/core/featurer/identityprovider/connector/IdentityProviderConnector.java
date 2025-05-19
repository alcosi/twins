package org.twins.core.featurer.identityprovider.connector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_33,
        name = "Identity provider connector",
        description = "")
@Slf4j
public abstract class IdentityProviderConnector extends FeaturerTwins {
    public ClientSideAuthData login(HashMap<String, String> identityProviderConnectorParams, String username, String password, String fingerprint) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return login(properties, username, password, fingerprint);
    }

    protected abstract ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException;

    public ClientSideAuthData refresh(HashMap<String, String> identityProviderConnectorParams, String refreshToken, String fingerprint) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return refresh(properties, refreshToken, fingerprint);
    }

    protected abstract ClientSideAuthData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException;

    public TokenMetaData resolveAuthTokenMetaData(HashMap<String, String> identityProviderConnectorParams, String token) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return resolveAuthTokenMetaData(properties, token);
    }

    protected abstract TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException;

    public List<AuthMethod> getSupportedMethods(HashMap<String, String> identityProviderConnectorParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return getSupportedMethods(properties);
    }

    public abstract List<AuthMethod> getSupportedMethods(Properties properties);

    public void logout(HashMap<String, String> identityProviderConnectorParams, ClientLogoutData logoutData) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        logout(properties, logoutData);
    }
    public abstract void logout(Properties properties, ClientLogoutData clientLogoutData) throws ServiceException;
}
