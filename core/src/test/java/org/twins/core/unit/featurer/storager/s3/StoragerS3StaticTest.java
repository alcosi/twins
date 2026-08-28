package org.twins.core.featurer.storager.s3;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class StoragerS3StaticTest extends BaseUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private ApiUser apiUser;

    @Mock
    private FeaturerService featurerService;

    private StoragerS3Static storager;

    @BeforeEach
    void setUp() throws Exception {
        storager = new StoragerS3Static();
        setField(storager, "authService", authService);
        setField(storager, "featurerService", featurerService);
        setField(storager, "contextPath", "");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private HashMap<String, String> buildParams() {
        var params = new HashMap<String, String>();
        params.put("selfHostDomainBaseUri", "/");
        params.put("basePath", "/twins-resources/{domainId}/{businessAccountId}");
        return params;
    }

    private void stubProperties(HashMap<String, String> params) throws ServiceException {
        var props = new Properties();
        params.forEach(props::setProperty);
        when(featurerService.extractProperties(storager, params)).thenReturn(props);
    }

    private void stubApiUser(UUID domainId, UUID businessAccountId) throws ServiceException {
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getDomainId()).thenReturn(domainId);
        when(apiUser.isBusinessAccountSpecified()).thenReturn(businessAccountId != null);
        if (businessAccountId != null) {
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
        }
    }

    @Nested
    class GetFileControllerUri {

        @Test
        void getFileControllerUri_withRootDomain_returnsPublicResourcePath() throws ServiceException {
            var params = buildParams();
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertEquals("//public/resource/{id}/v1", result);
        }

        @Test
        void getFileControllerUri_withDomainBaseUri_prependsDomain() throws ServiceException {
            var params = buildParams();
            params.put("selfHostDomainBaseUri", "https://twins.app");
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertEquals("https://twins.app//public/resource/{id}/v1", result);
        }

        @Test
        void getFileControllerUri_withDomainBaseUriTrailingSlash_noDoubleSlashes() throws ServiceException {
            var params = buildParams();
            params.put("selfHostDomainBaseUri", "https://twins.app/");
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertEquals("https://twins.app//public/resource/{id}/v1", result);
        }
    }

    @Nested
    class GenerateFileKey {

        @Test
        void generateFileKey_withDomainAndBusinessAccount_replacesPlaceholders() throws ServiceException {
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var fileId = UUID.randomUUID();
            stubApiUser(domainId, businessAccountId);

            var params = buildParams();
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertEquals("twins-resources/" + domainId + "/" + businessAccountId + "/" + fileId, result);
        }

        @Test
        void generateFileKey_withoutDomainAndBusinessAccount_usesDefaults() throws ServiceException {
            var fileId = UUID.randomUUID();
            stubApiUser(null, null);

            var params = buildParams();
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertEquals("twins-resources/defaultDomain/defaultBusinessAccount/" + fileId, result);
        }

        @Test
        void generateFileKey_withoutLeadingSlash_removesLeadingSlash() throws ServiceException {
            var domainId = UUID.randomUUID();
            var businessAccountId = UUID.randomUUID();
            var fileId = UUID.randomUUID();
            stubApiUser(domainId, businessAccountId);

            var params = buildParams();
            params.put("basePath", "twins-resources/{domainId}/{businessAccountId}");
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertEquals("twins-resources/" + domainId + "/" + businessAccountId + "/" + fileId, result);
        }
    }

    @Nested
    class AddFileInternal {

        @Test
        void addFileInternal_withInvalidS3Config_throwsServiceException() throws ServiceException {
            var params = new HashMap<String, String>();
            params.put("s3Uri", "http://nonexistent:9000");
            params.put("s3Region", "aws-global");
            params.put("s3Bucket", "test-bucket");
            params.put("s3AccessKey", "access");
            params.put("s3SecretKey", "secret");
            stubProperties(params);

            var content = "test content";
            var inputStream = new ByteArrayInputStream(content.getBytes());

            assertThrows(ServiceException.class,
                    () -> storager.addFileInternal("test-key", inputStream, "text/plain", params));
        }
    }

    @Nested
    class DeleteFile {

        @Test
        void deleteFile_withInvalidConfig_throwsServiceException() throws ServiceException {
            var params = new HashMap<String, String>();
            params.put("s3Uri", "http://nonexistent:9000");
            params.put("s3Region", "aws-global");
            params.put("s3Bucket", "test-bucket");
            params.put("s3AccessKey", "access");
            params.put("s3SecretKey", "secret");
            stubProperties(params);

            assertThrows(ServiceException.class,
                    () -> storager.deleteFile("someKey", params));
        }
    }

    @Nested
    class GetFileAsStream {

        @Test
        void getFileAsStream_withInvalidConfig_throwsServiceException() throws ServiceException {
            var params = new HashMap<String, String>();
            params.put("s3Uri", "http://nonexistent:9000");
            params.put("s3Region", "aws-global");
            params.put("s3Bucket", "test-bucket");
            params.put("s3AccessKey", "access");
            params.put("s3SecretKey", "secret");
            stubProperties(params);

            assertThrows(ServiceException.class,
                    () -> storager.getFileAsStream("someKey", params));
        }
    }
}
