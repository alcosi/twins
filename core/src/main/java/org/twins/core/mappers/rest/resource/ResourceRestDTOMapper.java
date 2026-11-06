package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dto.rest.resource.ResourceDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.resource.ResourceUploadService;

@Component
@RequiredArgsConstructor
public class ResourceRestDTOMapper extends RestSimpleDTOMapper<ResourceEntity, ResourceDTOv1> {

    @Override
    public void map(ResourceEntity src, ResourceDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setDomainId(src.getDomainId())
                .setUploadedByUserId(src.getUploadedByUserId())
                .setOriginalFileName(src.getOriginalFileName())
                .setSizeInBytes(src.getSizeInBytes())
                .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null)
                .setDownloadUri(String.format(ResourceUploadService.DOWNLOAD_URI_TEMPLATE, src.getId()));
    }

}
