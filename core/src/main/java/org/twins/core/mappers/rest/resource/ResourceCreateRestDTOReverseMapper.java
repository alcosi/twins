package org.twins.core.mappers.rest.resource;

import lombok.RequiredArgsConstructor;
import org.cambium.common.file.FileData;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.resource.ResourceCreate;
import org.twins.core.dto.rest.resource.ResourceCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

/**
 * The controller pre-resolves the multipart parts into {@link ResourceCreateDTOv1#file}
 * (web-layer concern); this mapper does the rest of the storageLink branching.
 */
@Component
@RequiredArgsConstructor
public class ResourceCreateRestDTOReverseMapper extends RestSimpleDTOMapper<ResourceCreateDTOv1, ResourceCreate> {
    private static final String MULTIPART_SCHEME = "multipart://";

    @Override
    public void map(ResourceCreateDTOv1 src, ResourceCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setTitle(src.getTitle())
                .setIsPublic(src.getIsPublic());
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
