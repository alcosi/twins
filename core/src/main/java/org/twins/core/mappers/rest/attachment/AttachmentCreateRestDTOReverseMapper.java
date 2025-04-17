package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class AttachmentCreateRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentCreateDTOv1, TwinAttachmentEntity> {

    final AttachmentSaveRestDTOReverseMapper attachmentSaveRestDTOReverseMapper;

    @Override
    public void map(AttachmentCreateDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        attachmentSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinCommentId(src.getCommentId());
    }
}
