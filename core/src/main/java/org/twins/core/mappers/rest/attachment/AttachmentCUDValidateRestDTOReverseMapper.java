package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentCUDValidateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.attachment.AttachmentService;

@Component
@RequiredArgsConstructor
public class AttachmentCUDValidateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCUDValidateRqDTOv1, EntityCUD<TwinAttachmentEntity>> {
    private final AttachmentService attachmentService;
    private final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @Override
    public void map(AttachmentCUDValidateRqDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertCollection(src.getAttachments().getUpdate()))
                .setCreateList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getAttachments().getCreate()))
                .setDeleteList(attachmentService.findEntitiesSafe(src.getAttachments().getDelete()).getList());
    }
}
