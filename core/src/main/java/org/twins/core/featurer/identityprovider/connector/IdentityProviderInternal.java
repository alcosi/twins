package org.twins.core.featurer.identityprovider.connector;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.domain.auth.method.AuthMethodPassword;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.auth.IdentityProviderInternalService;

import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1902,
        name = "Internal simple identity realization",
        description = "Not for production purpose")
@RequiredArgsConstructor
public class IdentityProviderInternal extends IdentityProviderConnector {
    @FeaturerParam(name = "Auth token lifetime", description = "Auth token lifetime in seconds", order = 2)
    public static final FeaturerParamInt authTokenLifetimeInSeconds = new FeaturerParamInt("authTokenLifetimeInSeconds");

    @FeaturerParam(name = "Refresh token lifetime", description = "Refresh token lifetime in seconds", order = 2)
    public static final FeaturerParamInt refreshTokenLifetimeInSeconds = new FeaturerParamInt("refreshTokenLifetimeInSeconds");

    private final IdentityProviderInternalService identityProviderInternalService;

    @Override
    protected ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {
        return identityProviderInternalService.login(username, password, fingerprint, authTokenLifetimeInSeconds.extract(properties), refreshTokenLifetimeInSeconds.extract(properties));
    }

    @Override
    protected ClientSideAuthData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException{
        return identityProviderInternalService.refresh(refreshToken, fingerprint, authTokenLifetimeInSeconds.extract(properties), refreshTokenLifetimeInSeconds.extract(properties));
    }

    @Override
    protected TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException {
        return identityProviderInternalService.resolve(token);
    }

    @Override
    public List<AuthMethod> getSupportedMethods(Properties properties) {
        return List.of(new AuthMethodPassword()
                .setRegisterSupported(false)
                .setRecoverSupported(false)
                .setFingerprintRequired(false));
    }

    @Override
    public void logout(Properties properties, ClientLogoutData clientLogoutData) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_LOGOUT_NOT_SUPPORTED);
    }
}
