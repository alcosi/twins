package org.twins.core.featurer.identityprovider.connector;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1903,
        name = "ALCOSI IDS",
        description = "")
@RequiredArgsConstructor
public class IdentityProviderAlcosi extends IdentityProviderConnector {

    @Override
    protected ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED);
    }

    @Override
    protected ClientSideAuthData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException{
        throw new ServiceException(ErrorCodeTwins.IDP_TOKEN_REFRESH_NOT_SUPPORTED);
    }

    @Override
    protected TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_RESOLVE_TOKEN_NOT_SUPPORTED);
    }

    @Override
    public List<AuthMethod> getSupportedMethods(Properties properties) {
        return List.of();
    }

    @Override
    public void logout(Properties properties, ClientLogoutData clientLogoutData) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_LOGOUT_NOT_SUPPORTED);
    }

    @Override
    public AuthSignup.Result signup(Properties properties, AuthSignup authSignup) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_SIGNUP_NOT_SUPPORTED);
    }
}
