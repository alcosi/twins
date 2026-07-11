package org.twins.core.featurer.storager.filehandler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


class StoragerAlcosiFileHandlerV2Test extends BaseUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthService authService;

    @Mock
    private ApiUser apiUser;

    @Mock
    private FeaturerService featurerService;

    private StoragerAlcosiFileHandlerV2 storager;

    @BeforeEach
    void setUp() throws Exception {
        storager = new StoragerAlcosiFileHandlerV2(restTemplate);
        setField(storager, "authService", authService);
        setField(storager, "featurerService", featurerService);
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
        params.put("fileHandlerUri", "http://localhost:8011");
        params.put("fileHandlerUploadPath", "/api/resize/save/synced");
        params.put("relativePath", "/{businessAccountId}/{fileId}");
        params.put("basePathReplaceMap", "{\"oldPath\":\"newPath\"}");
        return params;
    }

    private void stubProperties(HashMap<String, String> params) throws ServiceException {
        var props = new Properties();
        params.forEach(props::setProperty);
        when(featurerService.extractProperties(eq(storager), any(HashMap.class))).thenReturn(props);
    }

    private void stubApiUser(UUID domainId, UUID businessAccountId) throws ServiceException {
        when(authService.getApiUser()).thenReturn(apiUser);
        if (domainId != null) {
            when(apiUser.getDomainId()).thenReturn(domainId);
        }
        when(apiUser.isBusinessAccountSpecified()).thenReturn(businessAccountId != null);
        if (businessAccountId != null) {
            when(apiUser.getBusinessAccountId()).thenReturn(businessAccountId);
        }
    }

    @Nested
    class GetFileControllerUri {

        @Test
        void getFileControllerUri_returnsEmptyString() throws ServiceException {
            var result = storager.getFileControllerUri(new HashMap<>());

            assertEquals("", result);
        }
    }

    @Nested
    class GetFileUri {

        @Test
        void getFileUri_returnsUriFromKey() throws ServiceException {
            var fileId = UUID.randomUUID();
            var params = new HashMap<String, String>();

            var result = storager.getFileUri(fileId, "https://cdn.example.com/file.webp", params);

            assertEquals(java.net.URI.create("https://cdn.example.com/file.webp"), result);
        }
    }

    @Nested
    class GetFileAsStream {

        @Test
        void getFileAsStream_throwsServiceException() {
            var params = new HashMap<String, String>();

            var ex = assertThrows(ServiceException.class,
                    () -> storager.getFileAsStream("someKey", params));
            assertTrue(ex.getMessage().contains("File handler service is not configured to store file bytes"));
        }
    }

    @Nested
    class GenerateFileKey {

        @Test
        void generateFileKey_withBusinessAccount_replacesPlaceholders() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var fileId = UUID.randomUUID();
            stubApiUser(null, businessAccountId);

            var params = buildParams();
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertEquals(businessAccountId + "/" + fileId + "/" + fileId, result);
        }

        @Test
        void generateFileKey_withoutBusinessAccount_usesDefault() throws ServiceException {
            var fileId = UUID.randomUUID();
            when(authService.getApiUser()).thenReturn(apiUser);
            when(apiUser.isBusinessAccountSpecified()).thenReturn(false);

            var params = buildParams();
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertEquals("defaultBusinessAccount/" + fileId + "/" + fileId, result);
        }
    }

    @Nested
    class DeleteFile {

        @Test
        void deleteFile_successfulResponse_doesNotThrow() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            stubApiUser(domainId, businessAccountId);

            var params = buildParams();
            stubProperties(params);

            when(restTemplate.exchange(
                    anyString(),
                    any(),
                    any(),
                    eq(Void.class)
            )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

            assertDoesNotThrow(() -> storager.deleteFile("businessAccount/fileId/file.png", params));
        }

        @Test
        void deleteFile_failedResponse_throwsServiceException() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            stubApiUser(domainId, businessAccountId);

            var params = buildParams();
            stubProperties(params);

            when(restTemplate.exchange(
                    anyString(),
                    any(),
                    any(),
                    eq(Void.class)
            )).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

            assertThrows(ServiceException.class,
                    () -> storager.deleteFile("businessAccount/fileId/file.png", params));
        }

        @Test
        void deleteFile_restTemplateThrows_throwsServiceException() throws ServiceException {
            var businessAccountId = UUID.randomUUID();
            var domainId = UUID.randomUUID();
            stubApiUser(domainId, businessAccountId);

            var params = buildParams();
            stubProperties(params);

            when(restTemplate.exchange(
                    anyString(),
                    any(),
                    any(),
                    eq(Void.class)
            )).thenThrow(new RuntimeException("Connection refused"));

            assertThrows(ServiceException.class,
                    () -> storager.deleteFile("businessAccount/fileId/file.png", params));
        }
    }

    @Nested
    class PrepareObjectLink {

        @Test
        void prepareObjectLink_nullObjectLink_throwsServiceException() throws ServiceException {
            var params = buildParams();
            stubProperties(params);

            assertThrows(ServiceException.class,
                    () -> storager.addFileInternal("test-key", new java.io.ByteArrayInputStream(new byte[0]), "image/png", params));
        }

        @Test
        void prepareObjectLink_multipleReplacements_appliesAll() throws Exception {
            var props = new Properties();
            props.setProperty("basePathReplaceMap", "{\"old-bucket\":\"new-bucket\",\"old-region\":\"new-region\"}");

            var method = StoragerAlcosiFileHandlerV2.class.getDeclaredMethod("prepareObjectLink", String.class, Properties.class);
            method.setAccessible(true);

            var result = (String) method.invoke(storager, "https://old-bucket.old-region.example.com/file.png", props);

            assertEquals("https://new-bucket.new-region.example.com/file.png", result);
        }

        @Test
        void prepareObjectLink_singleReplacement_appliesOnce() throws Exception {
            var props = new Properties();
            props.setProperty("basePathReplaceMap", "{\"oldPath\":\"newPath\"}");

            var method = StoragerAlcosiFileHandlerV2.class.getDeclaredMethod("prepareObjectLink", String.class, Properties.class);
            method.setAccessible(true);

            var result = (String) method.invoke(storager, "https://oldPath/file.png", props);

            assertEquals("https://newPath/file.png", result);
        }
    }
}
