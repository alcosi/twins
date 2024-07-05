package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentBaseDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class AttachmentBaseRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentBaseDTOv1, TwinAttachmentEntity> {

    @Override
    public void map(AttachmentBaseDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setStorageLink(src.getStorageLink())
                .setTitle(src.getTitle())
                .setDescription(src.getDescription())
                .setExternalId(src.getExternalId());
    }
}
