package org.twins.core.service.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;

import java.util.ArrayList;
import java.util.List;

/**
 * Standalone file upload, not bound to a twin. Accepts the same multipart contract as the twin
 * attachment flow (json "request" part with {@code storageLink: "multipart://<part_name>"} refs +
 * raw file parts), but unlike AttachmentService it performs no validation at all: no permission
 * check, no attachment restrictions, no size/count limits, no bean validation - every sent file is
 * stored as a resource row. The caller's domain (DomainId header) and identity are resolved by the
 * standard ApiUser flow inside {@link ResourceService#addResource(String, java.io.InputStream)} -
 * one transaction per file, so a failure in the middle of the batch leaves the already uploaded
 * files stored. The controller maps stored rows to the resource response DTO.
 *
 * <p>File bytes travel as {@link FileData} ({@code AttachmentCreateDTOv1#domainFile} after
 * {@code processAttribute}, or a raw multipart part). A non-multipart {@code storageLink} is stored
 * as an external uri.
 */
@Service
@RequiredArgsConstructor
public class ResourceUploadService {
    private static final String MULTIPART_SCHEME = "multipart://";
    private final ResourceService resourceService;

    public List<ResourceEntity> upload(List<FileData> files) throws ServiceException {
        List<ResourceEntity> stored = new ArrayList<>();
        if (files != null) {
            for (FileData file : files)
                stored.add(resourceService.addResource(file.originalFileName(), file.content()));
        }
        return stored;
    }

    /**
     * Attachments must already be passed through {@code AttachmentCreateRestDTOReverseMapper#preProcessAttachments},
     * so a multipart link is a {@link FileData} on {@code domainFile}.
     */
    public List<ResourceEntity> uploadAttachments(List<AttachmentCreateDTOv1> attachments) throws ServiceException {
        List<ResourceEntity> stored = new ArrayList<>();
        if (attachments != null) {
            for (AttachmentCreateDTOv1 attachment : attachments)
                stored.add(storeAttachment(attachment));
        }
        return stored;
    }

    private ResourceEntity storeAttachment(AttachmentCreateDTOv1 attachment) throws ServiceException {
        FileData file = attachment.getDomainFile();
        if (file != null) {
            return resourceService.addResource(
                    fileName(attachment.getTitle(), file.originalFileName(), attachment.getStorageLink()),
                    file.content());
        }
        return resourceService.addResource(
                fileName(attachment.getTitle(), null, attachment.getStorageLink()),
                attachment.getStorageLink());
    }

    private String fileName(String title, String originalFileName, String storageLink) {
        if (StringUtils.isNotEmpty(title))
            return title;
        if (StringUtils.isNotEmpty(originalFileName))
            return originalFileName;
        if (storageLink != null && storageLink.toLowerCase().startsWith(MULTIPART_SCHEME))
            return storageLink.substring(MULTIPART_SCHEME.length());
        return storageLink;
    }
}
