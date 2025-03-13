package org.twins.core.mappers.rest.comment;

import org.springframework.stereotype.Component;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class CommentRestDTOReversedMapper extends RestSimpleDTOMapper<CommentDTOv1, TwinCommentEntity> {

    @Override
    public void map(CommentDTOv1 src, TwinCommentEntity dst, MapperContext mapperContext) throws Exception {
                dst.setText(src.getText());
    }
}
