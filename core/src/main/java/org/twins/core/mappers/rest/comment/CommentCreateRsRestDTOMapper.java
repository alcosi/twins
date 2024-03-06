package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.comment.CommentCreateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.comment.CommentService;

@Component
@RequiredArgsConstructor
public class CommentCreateRsRestDTOMapper extends RestSimpleDTOMapper<CommentService.CommentCreateResult, CommentCreateRsDTOv1> {
    @Override
    public void map(CommentService.CommentCreateResult src, CommentCreateRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setCommentId(src.getCommentId())
                .setAttachmentListId(src.getAttachments());
    }
}
