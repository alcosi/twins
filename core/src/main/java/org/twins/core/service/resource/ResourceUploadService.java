package org.twins.core.service.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.domain.resource.ResourceUploadItem;
import org.twins.core.dto.rest.resource.ResourceUploadDTOv1;
import org.twins.core.dto.rest.resource.ResourceUploadRsDTOv1;

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
 * files stored. Deliberately returns response DTOs instead of entities: there is no resource mapper
 * stack yet, and the controller must not reference dao classes.
 */
@Service
@RequiredArgsConstructor
public class ResourceUploadService {
    public static final String DOWNLOAD_URI_TEMPLATE = "/public/resource/%s/v1";

    private final ResourceService resourceService;

    public ResourceUploadRsDTOv1 uploadResources(List<ResourceUploadItem> items) throws ServiceException {
        ResourceUploadRsDTOv1 rs = new ResourceUploadRsDTOv1();
        List<ResourceUploadDTOv1> uploadedFiles = new ArrayList<>();
        if (items != null) {
            for (ResourceUploadItem item : items) {
                var resource = item.getExternalUri() != null
                        ? resourceService.addResource(item.getFileName(), item.getExternalUri())
                        : resourceService.addResource(item.getFileName(), item.getContent());
                uploadedFiles.add(convertToDto(resource));
            }
        }
        rs.setFiles(uploadedFiles);
        return rs;
    }

    private ResourceUploadDTOv1 convertToDto(ResourceEntity resource) throws ServiceException {
        return new ResourceUploadDTOv1()
                .setResourceId(resource.getId())
                .setOriginalFileName(resource.getOriginalFileName())
                .setSizeInBytes(resource.getSizeInBytes())
                .setStorageLink(resourceService.getResourceUri(resource))
                .setDownloadUri(String.format(DOWNLOAD_URI_TEMPLATE, resource.getId()));
    }
}
