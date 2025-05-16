package org.twins.core.featurer.identityprovider.connector;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientTokenData;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_33,
        name = "Identity provider connector",
        description = "")
@Slf4j
public abstract class IdentityProviderConnector extends FeaturerTwins {
    public ClientTokenData login(HashMap<String, String> identityProviderConnectorParams, String username, String password, String fingerprint) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return login(properties, username, password, fingerprint);
    }

    protected abstract ClientTokenData login(Properties properties, String username, String password, String fingerprint) throws ServiceException;

    public ClientTokenData refresh(HashMap<String, String> identityProviderConnectorParams, String refreshToken, String fingerprint) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return refresh(properties, refreshToken, fingerprint);
    }

    protected abstract ClientTokenData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException;

    public TokenMetaData resolveAuthTokenMetaData(HashMap<String, String> initiatorParams, String token) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams, new HashMap<>());
        return resolveAuthTokenMetaData(properties, token);
    }

    protected abstract TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException;
}
