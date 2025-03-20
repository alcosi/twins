package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentCreateDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class CommentCreateRestDTOReversedMapper extends RestSimpleDTOMapper<CommentCreateDTOv1, TwinCommentEntity> {
    private final CommentSaveRestDTOReversedMapper commentSaveRestDTOReversedMapper;

    @Override
    public void map(CommentCreateDTOv1 src, TwinCommentEntity dst, MapperContext mapperContext) throws Exception {
        commentSaveRestDTOReversedMapper.map(src, dst, mapperContext);
    }
}
