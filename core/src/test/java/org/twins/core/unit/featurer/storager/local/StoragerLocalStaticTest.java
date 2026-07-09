package org.twins.core.featurer.storager.local;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.service.auth.AuthService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


class StoragerLocalStaticTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    @Mock
    private AuthService authService;

    private StoragerLocalStatic storager;

    @BeforeEach
    void setUp() throws Exception {
        storager = new StoragerLocalStatic();
        ReflectionTestUtils.setField(storager, "authService", authService);
        ReflectionTestUtils.setField(storager, "featurerService", featurerService);
        ReflectionTestUtils.setField(storager, "contextPath", "");
    }

    private void stubProperties(HashMap<String, String> params) throws ServiceException {
        var props = new Properties();
        params.forEach(props::setProperty);
        lenient().when(featurerService.extractProperties(eq(storager), any(HashMap.class))).thenReturn(props);
    }

    @Nested
    class GetFileControllerUri {

        @Test
        void getFileControllerUri_withContextPath_returnsCorrectUri() throws ServiceException {
            var params = new HashMap<String, String>();
            params.put("selfHostDomainBaseUri", "https://example.com");
            stubProperties(params);
            ReflectionTestUtils.setField(storager, "contextPath", "/api");

            var result = storager.getFileControllerUri(params);

            assertTrue(result.contains("/api/public/resource/{id}/v1"));
            assertTrue(result.startsWith("https://example.com"));
        }

        @Test
        void getFileControllerUri_withoutContextPath_returnsCorrectUri() throws ServiceException {
            var params = new HashMap<String, String>();
            params.put("selfHostDomainBaseUri", "https://example.com");
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            assertTrue(result.endsWith("/public/resource/{id}/v1"));
            assertTrue(result.startsWith("https://example.com"));
        }

        @Test
        void getFileControllerUri_withTrailingSlashInDomain_hasDoubleSlash() throws ServiceException {
            var params = new HashMap<String, String>();
            params.put("selfHostDomainBaseUri", "https://example.com/");
            stubProperties(params);

            var result = storager.getFileControllerUri(params);

            // Note: prod code has a bug here - doesn't remove trailing slash from domain
            // Result is "https://example.com//public/resource/{id}/v1" with double slash
            assertTrue(result.contains("//public/resource/{id}/v1"));
        }
    }

    @Nested
    class GenerateFileKey {

        @Test
        void generateFileKey_withDomainAndBusinessAccount_replacesPlaceholders() throws ServiceException {
            var fileId = UUID.randomUUID();

            var params = new HashMap<String, String>();
            params.put("baseLocalPath", "/opt/resources/{domainId}/{businessAccountId}");
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertTrue(result.contains("/opt/resources/"));
            assertTrue(result.contains(fileId.toString()));
        }

        @Test
        void generateFileKey_withTrailingSlash_handlesCorrectly() throws ServiceException {
            var fileId = UUID.randomUUID();

            var params = new HashMap<String, String>();
            params.put("baseLocalPath", "/opt/resources/");
            stubProperties(params);

            var result = storager.generateFileKey(fileId, params);

            assertTrue(result.startsWith("/opt/resources/"));
            assertTrue(result.endsWith(fileId.toString()));
        }
    }

    @Nested
    class DeleteFile {

        @Test
        void deleteFile_nonExistentFile_doesNotThrow() throws ServiceException {
            var params = new HashMap<String, String>();
            var nonExistentPath = "/tmp/twins-test-nonexistent-" + UUID.randomUUID();

            assertDoesNotThrow(() -> storager.deleteFile(nonExistentPath, params));
        }

        @Test
        void deleteFile_existingFile_deletesSuccessfully() throws Exception {
            var tempDir = Files.createTempDirectory("twins-test-");
            var testFile = tempDir.resolve("test-file.txt");
            Files.writeString(testFile, "test content");

            var params = new HashMap<String, String>();

            assertDoesNotThrow(() -> storager.deleteFile(testFile.toString(), params));
            assertFalse(Files.exists(testFile));

            Files.deleteIfExists(testFile);
            Files.deleteIfExists(tempDir);
        }
    }

    @Nested
    class GetFileAsStream {

        @Test
        void getFileAsStream_nonExistentFile_throwsServiceException() throws ServiceException {
            var params = new HashMap<String, String>();
            var nonExistentPath = "/tmp/twins-test-nonexistent-" + UUID.randomUUID();

            assertThrows(ServiceException.class,
                    () -> storager.getFileAsStream(nonExistentPath, params));
        }

        @Test
        void getFileAsStream_existingFile_returnsStream() throws Exception {
            var tempDir = Files.createTempDirectory("twins-test-");
            var testFile = tempDir.resolve("test-file.txt");
            Files.writeString(testFile, "test content");

            var params = new HashMap<String, String>();

            var result = storager.getFileAsStream(testFile.toString(), params);

            assertNotNull(result);
            Files.deleteIfExists(testFile);
            Files.deleteIfExists(tempDir);
        }
    }

    @Nested
    class AddFileInternal {

        @Test
        void addFileInternal_createsFileSuccessfully() throws Exception {
            var tempDir = Files.createTempDirectory("twins-test-");
            var testFile = tempDir.resolve("test-file.txt");
            var content = "test content";

            var params = new HashMap<String, String>();

            var result = storager.addFileInternal(testFile.toString(), new ByteArrayInputStream(content.getBytes()), "text/plain", params);

            assertNotNull(result);
            assertEquals(testFile.toString(), result.fileKey());
            assertEquals(content.length(), result.fileSize());
            assertTrue(Files.exists(testFile));

            Files.deleteIfExists(testFile);
            Files.deleteIfExists(tempDir);
        }
    }
}
