package org.twins.core.featurer.identityprovider.connector;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.auth.AuthSignup;
import org.twins.core.domain.auth.method.AuthMethodStub;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.identityprovider.ClientLogoutData;
import org.twins.core.service.HttpRequestService;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class IdentityProviderStubTest extends BaseUnitTest {

    @Mock
    private HttpRequestService httpRequestService;

    private IdentityProviderStub identityProviderStub;

    @BeforeEach
    void setUp() {
        identityProviderStub = new IdentityProviderStub(httpRequestService);
    }

    @Nested
    class Login {

        @Test
        void login_throwsServiceException() {
            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.login(new Properties(), "user", "pass", "fp"));

            assertEquals(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class M2mAuth {

        @Test
        void m2mAuth_throwsServiceException() {
            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.m2mAuth(new Properties(), "clientId", "clientSecret"));

            assertEquals(ErrorCodeTwins.IDP_PASSWORD_LOGIN_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class Refresh {

        @Test
        void refresh_throwsServiceException() {
            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.refresh(new Properties(), "refreshToken", "fp"));

            assertEquals(ErrorCodeTwins.IDP_TOKEN_REFRESH_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class ResolveAuthTokenMetaData {

        @Test
        void resolveAuthTokenMetaData_withSingleElementToken_parsesBusinessAccountId() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var token = businessAccountId.toString();

            var result = identityProviderStub.resolveAuthTokenMetaData(new Properties(), token);

            assertEquals(businessAccountId, result.getBusinessAccountId());
            assertNull(result.getUserId());
        }

        @Test
        void resolveAuthTokenMetaData_withBusinessAccountIdAndUserId_parsesBoth() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var token = businessAccountId + "," + userId;

            var result = identityProviderStub.resolveAuthTokenMetaData(new Properties(), token);

            assertEquals(businessAccountId, result.getBusinessAccountId());
            assertEquals(userId, result.getUserId());
        }

        @Test
        void resolveAuthTokenMetaData_withEmptyToken_usesHttpRequestServiceHeaders() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            when(httpRequestService.getBusinessAccountIdFromRequest()).thenReturn(businessAccountId.toString());
            when(httpRequestService.getUserIdFromRequest()).thenReturn(userId.toString());

            var result = identityProviderStub.resolveAuthTokenMetaData(new Properties(), "");

            assertEquals(businessAccountId, result.getBusinessAccountId());
            assertEquals(userId, result.getUserId());
            verify(httpRequestService).getBusinessAccountIdFromRequest();
            verify(httpRequestService).getUserIdFromRequest();
        }

        @Test
        void resolveAuthTokenMetaData_withNullToken_usesHttpRequestServiceHeaders() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            when(httpRequestService.getBusinessAccountIdFromRequest()).thenReturn(businessAccountId.toString());
            when(httpRequestService.getUserIdFromRequest()).thenReturn(userId.toString());

            var result = identityProviderStub.resolveAuthTokenMetaData(new Properties(), null);

            assertEquals(businessAccountId, result.getBusinessAccountId());
            assertEquals(userId, result.getUserId());
        }
    }

    @Nested
    class GetSupportedMethods {

        @Test
        void getSupportedMethods_returnsStubMethod() {
            var methods = identityProviderStub.getSupportedMethods(new Properties());

            assertEquals(1, methods.size());
            assertInstanceOf(AuthMethodStub.class, methods.get(0));
        }
    }

    @Nested
    class Logout {

        @Test
        void logout_throwsServiceException() {
            var logoutData = new ClientLogoutData();

            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.logout(new Properties(), logoutData));

            assertEquals(ErrorCodeTwins.IDP_LOGOUT_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class SignupByEmailInitiate {

        @Test
        void signupByEmailInitiate_throwsServiceException() {
            var authSignup = new AuthSignup();

            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.signupByEmailInitiate(new Properties(), authSignup));

            assertEquals(ErrorCodeTwins.IDP_SIGNUP_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class SignupByEmailActivate {

        @Test
        void signupByEmailActivate_throwsServiceException() {
            var userId = UUID.randomUUID();

            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.signupByEmailActivate(new Properties(), userId, "email@test.com", "token"));

            assertEquals(ErrorCodeTwins.IDP_SIGNUP_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class SwitchActiveBusinessAccount {

        @Test
        void switchActiveBusinessAccount_throwsServiceException() {
            var authToken = "authToken";
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();

            var exception = assertThrows(ServiceException.class, () ->
                    identityProviderStub.switchActiveBusinessAccount(new Properties(), authToken, domainId, businessAccountId));

            assertEquals(ErrorCodeTwins.IDP_SWITCH_ACTIVE_BUSINESS_ACCOUNT_NOT_SUPPORTED.getCode(), exception.getErrorCode());
        }
    }
}
