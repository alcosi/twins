package org.twins.core.mappers.rest.comment;

import org.springframework.stereotype.Component;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class CommentCreateRestDTOReversedMapper extends RestSimpleDTOMapper<CommentCreateDTOv1, TwinCommentEntity> {

    @Override
    public void map(CommentCreateDTOv1 src, TwinCommentEntity dst, MapperContext mapperContext) throws Exception {
        dst.setText(src.getText());
    }
}
