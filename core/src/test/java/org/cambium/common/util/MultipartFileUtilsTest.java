package org.cambium.common.util;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TWINS-934 batch multipart contract detectors: multipart:// link resolution and request file collection.
 */
class MultipartFileUtilsTest {

    private static final byte[] ICON_BYTES = "png-icon-content".getBytes(StandardCharsets.UTF_8);

    @Test
    void convertReturnsNullForNullAndEmptyFile() throws Exception {
        assertNull(MultipartFileUtils.convert(null));
        assertNull(MultipartFileUtils.convert(new MockMultipartFile("icon", new byte[0])));
    }

    @Test
    void resolveMultipartFileReturnsNullForAbsentBlankAndExternalLinks() throws ServiceException {
        Map<String, MultipartFile> files = Map.of("icon", new MockMultipartFile("icon", ICON_BYTES));
        assertNull(MultipartFileUtils.resolveMultipartFile(null, files));
        assertNull(MultipartFileUtils.resolveMultipartFile("  ", files));
        // external links are the caller's concern, not a multipart reference
        assertNull(MultipartFileUtils.resolveMultipartFile("https://cdn.example.com/icon.png", files));
    }

    @Test
    void resolveMultipartFileThrowsWhenReferencedPartIsMissing() {
        Map<String, MultipartFile> files = Collections.emptyMap();
        ServiceException se = assertThrows(ServiceException.class,
                () -> MultipartFileUtils.resolveMultipartFile("multipart://iconLight", files));
        assertEquals(ErrorCodeCommon.MULTIPART_FILE_IS_NOT_PRESENTED.getCode(), se.getErrorCode());
        assertTrue(se.getMessage().contains("iconLight"), "message should name the missing part");
    }

    @Test
    void resolveMultipartFileGivesIndependentStreamPerResolution() throws Exception {
        Map<String, MultipartFile> files = Map.of("icon", new MockMultipartFile("icon", "icon.png", "image/png", ICON_BYTES));
        FileData first = MultipartFileUtils.resolveMultipartFile("multipart://icon", files);
        FileData second = MultipartFileUtils.resolveMultipartFile("multipart://icon", files);
        // per-element resolution must hand out independent streams: reading one must not exhaust the other.
        // this is the regression guard for the batch-shared-InputStream data corruption
        assertNotSame(first.content(), second.content());
        assertArrayEquals(ICON_BYTES, first.content().readAllBytes());
        assertArrayEquals(ICON_BYTES, second.content().readAllBytes());
        assertEquals("icon.png", first.originalFileName());
        assertEquals(Long.valueOf(ICON_BYTES.length), first.fileSize());
    }

    @Test
    void collectFilesMapsPartsByNameWithLastWins() {
        MultipartFile requestPart = new MockMultipartFile("request", "{}".getBytes(StandardCharsets.UTF_8));
        MultipartFile firstDark = new MockMultipartFile("iconDark", "a".getBytes(StandardCharsets.UTF_8));
        MultipartFile lastDark = new MockMultipartFile("iconDark", "b".getBytes(StandardCharsets.UTF_8));
        MultipartHttpServletRequest request = mock(MultipartHttpServletRequest.class);
        when(request.getFileNames()).thenReturn(List.of("request", "iconDark").iterator());
        when(request.getFiles("request")).thenReturn(List.of(requestPart));
        when(request.getFiles("iconDark")).thenReturn(List.of(firstDark, lastDark));

        Map<String, MultipartFile> filesMap = MultipartFileUtils.collectFiles(request);

        assertEquals(2, filesMap.size());
        assertSame(lastDark, filesMap.get("iconDark"), "duplicate part names: last one wins (documented behavior)");
    }
}
