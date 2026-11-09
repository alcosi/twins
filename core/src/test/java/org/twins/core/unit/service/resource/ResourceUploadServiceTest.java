package org.twins.core.unit.service.resource;

import org.cambium.common.file.FileData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
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
        var storedFiles = service.upload(List.of(new FileData(new ByteArrayInputStream(new byte[3]), "renamed.png", 3L)));
        assertEquals(1, storedFiles.size());
        assertEquals(stored.getId(), storedFiles.get(0).getId());
        assertEquals("renamed.png", storedFiles.get(0).getOriginalFileName());
        assertEquals(3L, storedFiles.get(0).getSizeInBytes().longValue());
    }

    @Test
    void externalUri_downloadedViaUri() throws Exception {
        ResourceEntity stored = resource("doc.pdf", 5L);
        when(resourceService.addResource(eq("doc.pdf"), eq("https://external/file.pdf"))).thenReturn(stored);
        AttachmentCreateDTOv1 attachment = new AttachmentCreateDTOv1()
                .setTitle("doc.pdf")
                .setStorageLink("https://external/file.pdf");
        var storedFiles = service.uploadAttachments(List.of(attachment));
        assertEquals(stored.getId(), storedFiles.get(0).getId());
        verify(resourceService, never()).addResource(any(), any(InputStream.class));
    }

    @Test
    void attachmentFile_prefersTitleOverFileDataName() throws Exception {
        ResourceEntity stored = resource("renamed.png", 3L);
        when(resourceService.addResource(eq("renamed.png"), any(InputStream.class))).thenReturn(stored);
        AttachmentCreateDTOv1 attachment = new AttachmentCreateDTOv1()
                .setTitle("renamed.png")
                .setStorageLink("multipart://file0")
                .setDomainFile(new FileData(new ByteArrayInputStream(new byte[3]), "original.png", 3L));
        service.uploadAttachments(List.of(attachment));
        verify(resourceService).addResource(eq("renamed.png"), any(InputStream.class));
    }

    private ResourceEntity resource(String originalFileName, long size) {
        return new ResourceEntity()
                .setId(UUID.randomUUID())
                .setDomainId(domain.getId())
                .setOriginalFileName(originalFileName)
                .setSizeInBytes(size);
    }
}
