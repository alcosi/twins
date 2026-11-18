package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.file.FileData;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.resource.ResourceUpdate;
import org.twins.core.dto.rest.resource.ResourceUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

/**
 * The controller pre-resolves the multipart parts into {@link ResourceUpdateDTOv1#file}
 * (web-layer concern); this mapper does the rest of the storageLink branching.
 */
@Component
@RequiredArgsConstructor
public class ResourceUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ResourceUpdateDTOv1, ResourceUpdate> {
    private static final String MULTIPART_SCHEME = "multipart://";

    @Override
    public void map(ResourceUpdateDTOv1 src, ResourceUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setOriginalFileName(src.getOriginalFileName())
                .setIsPublic(src.getIsPublic());
        if (StringUtils.isEmpty(src.getStorageLink())) // no storageLink - keep the current file
            return;
        FileData file = src.getFile();
        if (file == null) // non-multipart storageLink - store as an external uri
            dst.setExternalResourceUri(src.getStorageLink());
        else {
            if (StringUtils.isEmpty(file.originalFileName())) // part sent without a filename - name it by the part name
                file = new FileData(file.content(), src.getStorageLink().substring(MULTIPART_SCHEME.length()), file.fileSize());
            dst.setFile(file);
        }
    }
}
