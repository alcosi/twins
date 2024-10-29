package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentCreateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class CommentCreateRsRestDTOMapper extends RestSimpleDTOMapper<TwinCommentEntity, CommentCreateRsDTOv1> {
    @Override
    public void map(TwinCommentEntity src, CommentCreateRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setCommentId(src.getId());
        if (src.getAttachmentKit() != null) {
            dst.setAttachmentListId(src
                    .getAttachmentKit().getCollection()
                    .stream()
                    .map(TwinAttachmentEntity::getId)
                    .toList());
        }
    }
}
