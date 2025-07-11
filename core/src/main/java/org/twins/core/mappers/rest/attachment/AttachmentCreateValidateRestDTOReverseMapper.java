package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.attachment.AttachmentCreateValidateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentCreateValidateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCreateValidateRqDTOv1, EntityCUD<TwinAttachmentEntity>> {
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @Override
    public void map(AttachmentCreateValidateRqDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setCreateList(attachmentCreateRestDTOReverseMapper.convertCollection(src.getCreate()));
    }
}
