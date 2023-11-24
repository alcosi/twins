package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentBaseRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentUpdateRestDTOReverseMapper;


@Component
@RequiredArgsConstructor
public class TwinAttachmentCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateDTOv1, EntityCUD<TwinAttachmentEntity>> {
    final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;
    @Override
    public void map(TwinUpdateDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setCreateList(attachmentAddRestDTOReverseMapper.convertList(src.getAttachmentsAdd()))
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertList(src.getAttachmentsUpdate()))
                .setDeleteUUIDList(src.getAttachmentsDelete());
    }
}
