package org.twins.core.mappers.rest.attachment;

import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentSaveDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
public class AttachmentSaveRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentSaveDTOv1, TwinAttachmentEntity> {

    @Override
    public void map(AttachmentSaveDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getTwinId())
                .setStorageLink(src.getStorageLink())
                .setModificationLinks(src.getStorageLinksMap())
                .setTitle(src.getTitle())
                //TODO set size as is.
                .setSize(src.getSize() == null ? 0 : src.getSize())
                .setDescription(src.getDescription())
                .setExternalId(src.getExternalId());
    }
}
