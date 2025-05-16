package org.twins.core.featurer.identityprovider.connector;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientTokenData;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_3301,
        name = "Stub",
        description = "")
@RequiredArgsConstructor
public class IdentityProviderStub extends IdentityProviderConnector {

    @Override
    protected ClientTokenData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED);
    }

    @Override
    protected ClientTokenData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException{
        throw new ServiceException(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED);
    }

    @Override
    protected TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException {
        String[] tokenData = token.split(",");
        TokenMetaData ret = new TokenMetaData()
                .setUserId(UUID.fromString(tokenData[0].trim()));
        if (tokenData.length > 1)
            ret.setBusinessAccountId(UUID.fromString(tokenData[1].trim()));
        return ret;
    }
}
