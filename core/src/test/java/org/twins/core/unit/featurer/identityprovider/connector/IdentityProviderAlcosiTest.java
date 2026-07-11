package org.twins.core.featurer.identityprovider.connector;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.auth.method.AuthMethodPassword;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


class IdentityProviderAlcosiTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    private IdentityProviderAlcosi identityProviderAlcosi;

    @BeforeEach
    void setUp() {
        identityProviderAlcosi = new IdentityProviderAlcosi(
                new org.springframework.web.client.RestTemplate(),
                new com.fasterxml.jackson.databind.ObjectMapper(),
                authService,
                Runnable::run);
    }

    private Properties buildProperties() {
        var properties = new Properties();
        properties.setProperty("identityServerTokenBaseUri", "http://localhost:8080");
        properties.setProperty("identityServerBaseUri", "http://localhost:8081");
        properties.setProperty("clientId", "testClientId");
        properties.setProperty("clientSecret", "testClientSecret");
        properties.setProperty("serviceScope", "openid");
        properties.setProperty("clientScope", "client");
        properties.setProperty("clientIntrospectionId", "introspectId");
        properties.setProperty("clientIntrospectionSecret", "introspectSecret");
        properties.setProperty("activeBusinessAccountClaimName", "businessAccount");
        return properties;
    }

    @Nested
    class GetSupportedMethods {

        @Test
        void getSupportedMethods_returnsPasswordMethodWithExpectedFlags() {
            var methods = identityProviderAlcosi.getSupportedMethods(new Properties());

            assertEquals(1, methods.size());
            var method = methods.get(0);
            assertInstanceOf(AuthMethodPassword.class, method);
            var passwordMethod = (AuthMethodPassword) method;
            assertFalse(passwordMethod.isRegisterSupported());
            assertFalse(passwordMethod.isRecoverSupported());
            assertFalse(passwordMethod.isFingerprintRequired());
        }
    }

    @Nested
    class Login {

        @Test
        void login_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.login(emptyProperties, "user", "pass", "fp"));
        }
    }

    @Nested
    class M2mAuth {

        @Test
        void m2mAuth_withEmptyProperties_throwsIllegalArgumentException() {
            var emptyProperties = new Properties();

            assertThrows(IllegalArgumentException.class, () ->
                    identityProviderAlcosi.m2mAuth(emptyProperties, "clientId", "clientSecret"));
        }
    }

    @Nested
    class Refresh {

        @Test
        void refresh_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.refresh(emptyProperties, "refreshToken", "fp"));
        }
    }

    @Nested
    class ResolveAuthTokenMetaData {

        @Test
        void resolveAuthTokenMetaData_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.resolveAuthTokenMetaData(emptyProperties, "token"));
        }
    }

    @Nested
    class Logout {

        @Test
        void logout_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();
            var logoutData = new ClientLogoutData();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.logout(emptyProperties, logoutData));
        }
    }

    @Nested
    class SignupByEmailInitiate {

        @Test
        void signupByEmailInitiate_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();
            var authSignup = new org.twins.core.domain.auth.AuthSignup();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.signupByEmailInitiate(emptyProperties, authSignup));
        }
    }

    @Nested
    class SignupByEmailActivate {

        @Test
        void signupByEmailActivate_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();
            var userId = UUID.randomUUID();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.signupByEmailActivate(emptyProperties, userId, "email@test.com", "token"));
        }
    }

    @Nested
    class SwitchActiveBusinessAccount {

        @Test
        void switchActiveBusinessAccount_withEmptyProperties_throwsNullPointerException() {
            var emptyProperties = new Properties();

            assertThrows(NullPointerException.class, () ->
                    identityProviderAlcosi.switchActiveBusinessAccount(
                            emptyProperties, "authToken", UUID.randomUUID(), UUID.randomUUID()));
        }
    }
}
