package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.comment.CommentUpdateRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentAddRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentUpdateRestDTOReverseMapper;

@Component
@RequiredArgsConstructor
public class CommentCudRestDTOReversedMapper extends RestSimpleDTOMapper<CommentUpdateRqDTOv1, EntityCUD<TwinAttachmentEntity>> {
    final AttachmentAddRestDTOReverseMapper attachmentAddRestDTOReverseMapper;
    final AttachmentUpdateRestDTOReverseMapper attachmentUpdateRestDTOReverseMapper;

    @Override
    public void map(CommentUpdateRqDTOv1 src, EntityCUD<TwinAttachmentEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(attachmentUpdateRestDTOReverseMapper.convertList(src.getAttachments().getUpdate()))
                .setCreateList(attachmentAddRestDTOReverseMapper.convertList(src.getAttachments().getCreate()))
                .setDeleteUUIDList(src.getAttachments().getDelete());
    }
}
