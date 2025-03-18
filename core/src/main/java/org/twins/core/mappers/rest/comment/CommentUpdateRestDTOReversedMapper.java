package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class CommentUpdateRestDTOReversedMapper extends RestSimpleDTOMapper<CommentUpdateDTOv1, TwinCommentEntity> {
    private final CommentSaveRestDTOReversedMapper commentSaveRestDTOReversedMapper;

    @Override
    public void map(CommentUpdateDTOv1 src, TwinCommentEntity dst, MapperContext mapperContext) throws Exception {
        commentSaveRestDTOReversedMapper.map(src, dst, mapperContext);
    }
}
