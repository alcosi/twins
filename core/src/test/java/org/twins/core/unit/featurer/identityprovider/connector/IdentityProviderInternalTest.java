package org.twins.core.featurer.identityprovider.connector;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.EmailVerificationByTwins;
import org.twins.core.domain.auth.method.AuthMethodPassword;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.featurer.identityprovider.ClientSideAuthData;
import org.twins.core.featurer.identityprovider.TokenMetaData;
import org.twins.core.service.auth.IdentityProviderInternalService;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class IdentityProviderInternalTest extends BaseUnitTest {

    @Mock
    private IdentityProviderInternalService identityProviderInternalService;

    private IdentityProviderInternal identityProviderInternal;

    @BeforeEach
    void setUp() {
        identityProviderInternal = new IdentityProviderInternal(identityProviderInternalService);
    }

    private Properties buildProperties() {
        var properties = new Properties();
        properties.setProperty(IdentityProviderInternal.authTokenLifetimeInSeconds.getKey(), "3600");
        properties.setProperty(IdentityProviderInternal.refreshTokenLifetimeInSeconds.getKey(), "86400");
        properties.setProperty(IdentityProviderInternal.m2mAuthTokenLifetimeInSeconds.getKey(), "7200");
        return properties;
    }

    @Nested
    class Login {

        @Test
        void login_delegatesToInternalService() throws ServiceException {
            var properties = buildProperties();
            var expected = new ClientSideAuthData();
            when(identityProviderInternalService.login("user", "pass", "fp", 3600, 86400))
                    .thenReturn(expected);

            var result = identityProviderInternal.login(properties, "user", "pass", "fp");

            assertSame(expected, result);
            verify(identityProviderInternalService).login("user", "pass", "fp", 3600, 86400);
        }
    }

    @Nested
    class M2mAuth {

        @Test
        void m2mAuth_delegatesToInternalService() throws ServiceException {
            var properties = buildProperties();
            var expected = new ClientSideAuthData();
            when(identityProviderInternalService.m2mToken("clientId", "clientSecret", 7200))
                    .thenReturn(expected);

            var result = identityProviderInternal.m2mAuth(properties, "clientId", "clientSecret");

            assertSame(expected, result);
            verify(identityProviderInternalService).m2mToken("clientId", "clientSecret", 7200);
        }
    }

    @Nested
    class Refresh {

        @Test
        void refresh_delegatesToInternalService() throws ServiceException {
            var properties = buildProperties();
            var expected = new ClientSideAuthData();
            when(identityProviderInternalService.refresh("refreshToken", "fp", 3600, 86400))
                    .thenReturn(expected);

            var result = identityProviderInternal.refresh(properties, "refreshToken", "fp");

            assertSame(expected, result);
            verify(identityProviderInternalService).refresh("refreshToken", "fp", 3600, 86400);
        }
    }

    @Nested
    class ResolveAuthTokenMetaData {

        @Test
        void resolveAuthTokenMetaData_delegatesToInternalService() throws ServiceException {
            var expected = new TokenMetaData();
            when(identityProviderInternalService.resolve("token123")).thenReturn(expected);

            var result = identityProviderInternal.resolveAuthTokenMetaData(new Properties(), "token123");

            assertSame(expected, result);
            verify(identityProviderInternalService).resolve("token123");
        }
    }

    @Nested
    class GetSupportedMethods {

        @Test
        void getSupportedMethods_returnsPasswordMethod() {
            var methods = identityProviderInternal.getSupportedMethods(new Properties());

            assertEquals(1, methods.size());
            var method = methods.get(0);
            assertInstanceOf(AuthMethodPassword.class, method);
            var passwordMethod = (AuthMethodPassword) method;
            assertTrue(passwordMethod.isRegisterSupported());
            assertFalse(passwordMethod.isRecoverSupported());
            assertFalse(passwordMethod.isFingerprintRequired());
        }
    }

    @Nested
    class Logout {

        @Test
        void logout_throwsServiceException() {
            var logoutData = new ClientLogoutData();

            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderInternal.logout(new Properties(), logoutData));

            assertEquals(ErrorCodeTwins.IDP_LOGOUT_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class SignupByEmailInitiate {

        @Test
        void signupByEmailInitiate_delegatesAndReturnsVerificationHolder() throws ServiceException {
            var authSignup = new AuthSignup()
                    .setTwinsUserId(UUID.randomUUID())
                    .setEmail("test@example.com")
                    .setPassword("password123");

            var result = identityProviderInternal.signupByEmailInitiate(new Properties(), authSignup);

            verify(identityProviderInternalService).signupByEmailInitiate(authSignup);
            assertInstanceOf(EmailVerificationByTwins.class, result);
            assertNotNull(((EmailVerificationByTwins) result).getIdpUserActivateCode());
        }
    }

    @Nested
    class SignupByEmailActivate {

        @Test
        void signupByEmailActivate_delegatesToInternalService() throws ServiceException {
            var twinsUserId = UUID.randomUUID();

            identityProviderInternal.signupByEmailActivate(new Properties(), twinsUserId, "email@test.com", "activateToken");

            verify(identityProviderInternalService).signupByEmailActivate(twinsUserId);
        }
    }

    @Nested
    class SwitchActiveBusinessAccount {

        @Test
        void switchActiveBusinessAccount_delegatesToInternalService() throws ServiceException {
            var authToken = "authToken123";
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();

            identityProviderInternal.switchActiveBusinessAccount(new Properties(), authToken, domainId, businessAccountId);

            verify(identityProviderInternalService).switchActiveBusinessAccount(authToken, domainId, businessAccountId);
        }
    }
}
