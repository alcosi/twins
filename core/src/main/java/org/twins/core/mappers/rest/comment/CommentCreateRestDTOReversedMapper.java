package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentCreateDTOv1;
import org.twins.core.mappers.rest.attachment.AttachmentCreateRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CommentCreateRestDTOReversedMapper extends RestSimpleDTOMapper<CommentCreateDTOv1, TwinCommentEntity> {
    private final CommentSaveRestDTOReversedMapper commentSaveRestDTOReversedMapper;
    private final AttachmentCreateRestDTOReverseMapper attachmentCreateRestDTOReverseMapper;

    @Override
    public void map(CommentCreateDTOv1 src, TwinCommentEntity dst, MapperContext mapperContext) throws Exception {
        commentSaveRestDTOReversedMapper.map(src, dst, mapperContext);
        dst
                .setTwinId(src.getTwinId())
                .setAttachmentKit(new Kit<>(src.getAttachmentEntities(), TwinAttachmentEntity::getId));
    }

    @Override
    public void beforeCollectionConversion(Collection<CommentCreateDTOv1> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        for (CommentCreateDTOv1 commentCreateDTOv1 : srcCollection) {
            commentCreateDTOv1.setAttachmentEntities(attachmentCreateRestDTOReverseMapper.convertCollection(commentCreateDTOv1.getAttachments(), mapperContext));
        }
    }
}
