package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dto.rest.attachment.AttachmentSaveDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Map;


@Component
@RequiredArgsConstructor
public class AttachmentSaveRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentSaveDTOv1, TwinAttachmentEntity> {

    @Override
    public void map(AttachmentSaveDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getTwinId())
                .setStorageFileKey(src.getStorageLink())
                .setModifications(new Kit<>(TwinAttachmentModificationEntity::getModificationType))
                .setTitle(src.getTitle())
                //TODO set size as is.
                .setSize(src.getSize() == null ? 0 : src.getSize())
                .setDescription(src.getDescription())
                .setExternalId(src.getExternalId());
        if (null != src.getModifications()) {
            for (Map.Entry<String, String> mod : src.getModifications().entrySet()) {
                TwinAttachmentModificationEntity modEntity = new TwinAttachmentModificationEntity();
                modEntity
                        .setModificationType(mod.getKey())
                        .setStorageFileKey(mod.getValue());
                dst.getModifications().add(modEntity);
            }
        }
    }
}
