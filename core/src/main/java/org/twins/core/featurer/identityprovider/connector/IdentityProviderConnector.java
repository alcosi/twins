package org.twins.core.featurer.identityprovider.connector;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.token.ClientTokenData;

import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;


@FeaturerType(id = FeaturerTwins.TYPE_33,
        name = "Identity provider connector",
        description = "")
@Slf4j
public abstract class IdentityProviderConnector extends FeaturerTwins {
    public ClientTokenData login(HashMap<String, String> identityProviderConnectorParams, String username, String password) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, identityProviderConnectorParams, new HashMap<>());
        return login(properties, username, password);
    }

    protected ClientTokenData login(Properties properties, String username, String password) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED);
    }

    @Data
    @Accessors(chain = true)
    public static class Result {
        UUID userId;
        UUID businessAccountId;
    }
}
