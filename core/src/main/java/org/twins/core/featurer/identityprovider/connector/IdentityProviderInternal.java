package org.twins.core.featurer.identityprovider.connector;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.EmailVerificationByTwins;
import org.twins.core.domain.auth.EmailVerificationHolder;
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
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1902,
        name = "Internal simple identity realization",
        description = "Not for production purpose")
@RequiredArgsConstructor
public class IdentityProviderInternal extends IdentityProviderConnector {
    @FeaturerParam(name = "Auth token lifetime", description = "Auth token lifetime in seconds", order = 1)
    public static final FeaturerParamInt authTokenLifetimeInSeconds = new FeaturerParamInt("authTokenLifetimeInSeconds");

    @FeaturerParam(name = "Refresh token lifetime", description = "Refresh token lifetime in seconds", order = 2)
    public static final FeaturerParamInt refreshTokenLifetimeInSeconds = new FeaturerParamInt("refreshTokenLifetimeInSeconds");

    @FeaturerParam(name = "M2M token lifetime", description = "M2M token lifetime in seconds", order = 1)
    public static final FeaturerParamInt m2mAuthTokenLifetimeInSeconds = new FeaturerParamInt("m2mAuthTokenLifetimeInSeconds");

    private final IdentityProviderInternalService identityProviderInternalService;

    @Override
    protected ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {
        return identityProviderInternalService.login(username, password, fingerprint, authTokenLifetimeInSeconds.extract(properties), refreshTokenLifetimeInSeconds.extract(properties));
    }

    @Override
    protected ClientSideAuthData m2mAuth(Properties properties, String clientId, String clientSecret) throws ServiceException {
        return identityProviderInternalService.m2mToken(clientId, clientSecret, m2mAuthTokenLifetimeInSeconds.extract(properties));
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
                .setRegisterSupported(true)
                .setRecoverSupported(false)
                .setFingerprintRequired(false));
    }

    @Override
    public void logout(Properties properties, ClientLogoutData clientLogoutData) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_LOGOUT_NOT_SUPPORTED);
    }

    @Override
    public EmailVerificationHolder signupByEmailInitiate(Properties properties, AuthSignup authSignup) throws ServiceException {
        identityProviderInternalService.signupByEmailInitiate(authSignup);
        return new EmailVerificationByTwins()
                .setIdpUserActivateCode(UUID.randomUUID().toString());
    }

    @Override
    public void signupByEmailActivate(Properties properties, UUID twinsUserId, String email, String idpUserActivateToken) throws ServiceException {
        identityProviderInternalService.signupByEmailActivate(twinsUserId);
    }

    @Override
    public void switchActiveBusinessAccount(Properties properties, String authToken, UUID domainId, UUID businessAccountId) throws ServiceException {
        identityProviderInternalService.switchActiveBusinessAccount(authToken, domainId, businessAccountId);
    }

}
