package org.twins.core.unit.service.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.domain.resource.ResourceUploadItem;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.resource.ResourceUploadService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceUploadServiceTest extends BaseUnitTest {

    @Mock
    private ResourceService resourceService;

    private ResourceUploadService service;
    private DomainEntity domain;

    @BeforeEach
    void setUp() {
        service = new ResourceUploadService(resourceService);
        domain = new DomainEntity().setId(UUID.randomUUID());
    }

    @Test
    void uploadHappyPath_storesEveryItem() throws Exception {
        ResourceEntity stored = resource("renamed.png", 3L);
        when(resourceService.addResource(eq("renamed.png"), any(InputStream.class))).thenReturn(stored);
        when(resourceService.getResourceUri(any(ResourceEntity.class))).thenReturn("https://storage/file");
        var rs = service.uploadResources(List.of(new ResourceUploadItem()
                .setFileName("renamed.png")
                .setContent(new ByteArrayInputStream(new byte[3]))));
        assertEquals(1, rs.getFiles().size());
        var first = rs.getFiles().get(0);
        assertEquals(stored.getId(), first.getResourceId());
        assertEquals("renamed.png", first.getOriginalFileName());
        assertEquals(3L, first.getSizeInBytes().longValue());
        assertEquals("https://storage/file", first.getStorageLink());
        assertEquals(String.format(ResourceUploadService.DOWNLOAD_URI_TEMPLATE, stored.getId()), first.getDownloadUri());
    }

    @Test
    void externalUri_downloadedViaUri() throws Exception {
        ResourceEntity stored = resource("doc.pdf", 5L);
        when(resourceService.addResource(eq("doc.pdf"), eq("https://external/file.pdf"))).thenReturn(stored);
        var rs = service.uploadResources(List.of(new ResourceUploadItem()
                .setFileName("doc.pdf")
                .setExternalUri("https://external/file.pdf")));
        assertEquals(stored.getId(), rs.getFiles().get(0).getResourceId());
        verify(resourceService, never()).addResource(any(), any(InputStream.class));
    }

    private ResourceEntity resource(String originalFileName, long size) {
        return new ResourceEntity()
                .setId(UUID.randomUUID())
                .setDomainId(domain.getId())
                .setOriginalFileName(originalFileName)
                .setSizeInBytes(size);
    }
}
