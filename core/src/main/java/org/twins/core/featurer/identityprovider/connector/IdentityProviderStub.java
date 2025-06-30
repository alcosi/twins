package org.twins.core.featurer.identityprovider.connector;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.EmailVerificationHolder;
import org.twins.core.domain.auth.method.AuthMethod;
import org.twins.core.domain.auth.method.AuthMethodStub;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.HttpRequestService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1901,
        name = "Stub",
        description = "Not for production purpose")
@RequiredArgsConstructor
public class IdentityProviderStub extends IdentityProviderConnector {
    final HttpRequestService httpRequestService;

    @Override
    protected ClientSideAuthData login(Properties properties, String username, String password, String fingerprint) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED);
    }

    @Override
    protected ClientSideAuthData m2mAuth(Properties properties, String clientId, String clientSecret) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED);
    }

    @Override
    protected ClientSideAuthData refresh(Properties properties, String refreshToken, String fingerprint) throws ServiceException{
        throw new ServiceException(ErrorCodeTwins.IDP_TOKEN_REFRESH_NOT_SUPPORTED);
    }

    @Override
    protected TokenMetaData resolveAuthTokenMetaData(Properties properties, String token) throws ServiceException {
        if (StringUtils.isEmpty(token))
            token = httpRequestService.getBusinessAccountIdFromRequest() + "," + httpRequestService.getUserIdFromRequest();
        String[] tokenData = token.split(",");
        TokenMetaData ret = new TokenMetaData()
                .setUserId(UUID.fromString(tokenData[0].trim()));
        if (tokenData.length > 1)
            ret.setBusinessAccountId(UUID.fromString(tokenData[1].trim()));
        return ret;
    }

    @Override
    public List<AuthMethod> getSupportedMethods(Properties properties) {
        return List.of(new AuthMethodStub());
    }

    @Override
    public void logout(Properties properties, ClientLogoutData clientLogoutData) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_LOGOUT_NOT_SUPPORTED);
    }

    @Override
    public EmailVerificationHolder signupByEmailInitiate(Properties properties, AuthSignup authSignup) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_SIGNUP_NOT_SUPPORTED);
    }

    @Override
    public void signupByEmailActivate(Properties properties, UUID twinsUserId, String email, String idpUserActivateToken) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_SIGNUP_NOT_SUPPORTED);
    }

    @Override
    public void switchActiveBusinessAccount(Properties properties, String authToken, UUID domainId, UUID businessAccountId) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.IDP_SWITCH_ACTIVE_BUSINESS_ACCOUNT_NOT_SUPPORTED);
    }
}
