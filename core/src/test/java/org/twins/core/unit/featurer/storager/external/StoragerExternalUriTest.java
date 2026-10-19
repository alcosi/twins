package org.twins.core.featurer.storager.external;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.base.BaseUnitTest;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;


class StoragerExternalUriTest extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    private final StoragerExternalUri storager = new StoragerExternalUri();

    @BeforeEach
    void setUp() throws ServiceException {
        ReflectionTestUtils.setField(storager, "featurerService", featurerService);
        var props = new Properties();
        lenient().when(featurerService.extractProperties(eq(storager), any())).thenReturn(props);
    }

    @Nested
    class GetFileControllerUri {

        @Test
        void getFileControllerUri_returnsEmptyString() throws ServiceException {
            var params = new HashMap<String, String>();

            var result = storager.getFileControllerUri(params);

            assertEquals("", result);
        }
    }

    @Nested
    class GenerateFileKey {

        @Test
        void generateFileKey_returnsEmptyString() throws ServiceException {
            var fileId = UUID.randomUUID();
            var params = new HashMap<String, String>();

            var result = storager.generateFileKey(fileId, params);

            assertEquals("", result);
        }
    }

    @Nested
    class DeleteFile {

        @Test
        void deleteFile_doesNotThrow() throws ServiceException {
            var params = new HashMap<String, String>();

            assertDoesNotThrow(() -> storager.deleteFile("someKey", params));
        }
    }

    @Nested
    class AddFileInternal {

        @Test
        void addFileInternal_throwsServiceException() throws ServiceException {
            var params = new HashMap<String, String>();

            var ex = assertThrows(ServiceException.class,
                    () -> storager.addFileInternal("key", null, "text/plain", params));
            assertTrue(ex.getMessage().contains("External URI service is not configured to store file bytes"));
        }
    }

    @Nested
    class AddExternalUrlFile {

        @Test
        void addExternalUrlFile_withValidUrl_returnsFileKey() throws ServiceException {
            var fileId = UUID.randomUUID();
            var externalUrl = "https://example.com/file.pdf";
            var params = new HashMap<String, String>();

            var result = storager.addExternalUrlFile(fileId, externalUrl, params);

            assertEquals(externalUrl, result.fileKey());
            assertEquals(-1, result.fileSize());
            assertTrue(result.modifications().isEmpty());
        }
    }

    @Nested
    class GetFileAsStream {

        @Test
        void getFileAsStream_throwsServiceExceptionForInvalidKey() throws ServiceException {
            var params = new HashMap<String, String>();

            assertThrows(ServiceException.class,
                    () -> storager.getFileAsStream("not-a-valid-url", params));
        }
    }

    @Nested
    class GetFileUri {

        @Test
        void getFileUri_returnsUriFromKey() throws ServiceException {
            var fileId = UUID.randomUUID();
            var params = new HashMap<String, String>();
            var expectedUri = java.net.URI.create("https://example.com/file.txt");

            var result = storager.getFileUri(fileId, "https://example.com/file.txt", params);

            assertEquals(expectedUri, result);
        }
    }
}
