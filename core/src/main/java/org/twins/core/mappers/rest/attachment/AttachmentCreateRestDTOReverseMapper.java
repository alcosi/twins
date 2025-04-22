package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class AttachmentCreateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCreateDTOv1, TwinAttachmentEntity> {

    final AttachmentSaveRestDTOReverseMapper attachmentSaveRestDTOReverseMapper;
    private final TwinService twinService;

    @Override
    public void map(AttachmentCreateDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        attachmentSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinCommentId(src.getCommentId());
    }

    @Override
    public void beforeCollectionConversion(Collection<AttachmentCreateDTOv1> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        List<UUID> twinIds = srcCollection.stream().map(AttachmentCreateDTOv1::getTwinId).toList();
        Kit<TwinEntity, UUID> dbTwinKit = twinService.findEntitiesSafe(twinIds);
        for (AttachmentCreateDTOv1 attachment : srcCollection) {
            attachment.setTwin(dbTwinKit.get(attachment.getTwinId()));
        }
    }
}
