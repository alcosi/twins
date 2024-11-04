package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class AttachmentAddRestDTOReverseMapper extends RestSimpleDTOMapper<AttachmentAddDTOv1, TwinAttachmentEntity> {

    final AttachmentBaseRestDTOReverseMapper attachmentBaseRestDTOReverseMapper;

    @Override
    public void map(AttachmentAddDTOv1 src, TwinAttachmentEntity dst, MapperContext mapperContext) throws Exception {
        attachmentBaseRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinCommentId(src.getCommentId());
    }
}
