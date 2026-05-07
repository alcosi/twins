package org.twins.core.featurer.identityprovider.trustor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TrustorEncryptedTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private ApiUser apiUser;

    private TrustorEncrypted trustorEncrypted;

    @BeforeEach
    void setUp() throws ServiceException {
        trustorEncrypted = new TrustorEncrypted(authService, new ObjectMapper());
    }

    private void stubApiUser(UUID domainId) throws ServiceException {
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(domainId);
    }

    @Nested
    class GetActAsUserPublicKey {

        @Test
        void getActAsUserPublicKey_returnsPublicKeyFromGeneratedKey() throws ServiceException {
            var domainId = UUID.randomUUID();
            stubApiUser(domainId);

            var result = trustorEncrypted.getActAsUserPublicKey(new Properties());

            assertNotNull(result);
            assertNotNull(result.getId());
            assertNotNull(result.getPublicKey());
            assertNotNull(result.getExpires());
        }

        @Test
        void getActAsUserPublicKey_cachesKeyForSameDomain() throws ServiceException {
            var domainId = UUID.randomUUID();
            stubApiUser(domainId);

            var first = trustorEncrypted.getActAsUserPublicKey(new Properties());
            var second = trustorEncrypted.getActAsUserPublicKey(new Properties());

            assertEquals(first.getId(), second.getId());
        }
    }

    @Nested
    class ResolveActAsUser {

        @Test
        void resolveActAsUser_withInvalidBase64_throwsIllegalArgumentException() {
            var invalidHeader = "!!!not-base64!!!";

            assertThrows(IllegalArgumentException.class, () ->
                    trustorEncrypted.resolveActAsUser(new Properties(), invalidHeader));
        }

        @Test
        void resolveActAsUser_withInvalidJsonPayload_throwsServiceException() {
            var payload = Base64.getEncoder().encodeToString("not-json".getBytes());

            var exception = assertThrows(ServiceException.class, () ->
                    trustorEncrypted.resolveActAsUser(new Properties(), payload));

            assertEquals(ErrorCodeTwins.ACT_AS_USER_INCORRECT.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class Decrypt {

        @Test
        void decrypt_withInvalidEncryptedKey_throwsServiceException() throws ServiceException {
            var domainId = UUID.randomUUID();
            stubApiUser(domainId);

            var invalidKey = "invalid-key-bytes".getBytes();
            var iv = new byte[12];
            var ciphertext = "invalid-ciphertext".getBytes();

            var exception = assertThrows(ServiceException.class, () ->
                    trustorEncrypted.decrypt(invalidKey, iv, ciphertext));

            assertEquals(ErrorCodeTwins.ACT_AS_USER_INCORRECT.getCode(), exception.getErrorCode());
        }
    }

    @Nested
    class GetKey {

        @Test
        void getKey_returnsValidCryptKey() throws ServiceException {
            var domainId = UUID.randomUUID();
            stubApiUser(domainId);

            var key = trustorEncrypted.getKey();

            assertNotNull(key);
            assertNotNull(key.getId());
            assertNotNull(key.getKeyPair());
            assertNotNull(key.getExpires());
        }

        @Test
        void getKey_cachesKeyForSameDomain() throws ServiceException {
            var domainId = UUID.randomUUID();
            stubApiUser(domainId);

            var first = trustorEncrypted.getKey();
            var second = trustorEncrypted.getKey();

            assertSame(first, second);
        }

        @Test
        void getKey_returnsDifferentKeyForDifferentDomains() throws ServiceException {
            var domainId1 = UUID.randomUUID();
            var domainId2 = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.getDomainId()).thenReturn(domainId1).thenReturn(domainId2);

            var first = trustorEncrypted.getKey();
            var second = trustorEncrypted.getKey();

            assertNotEquals(first.getId(), second.getId());
        }
    }
}
