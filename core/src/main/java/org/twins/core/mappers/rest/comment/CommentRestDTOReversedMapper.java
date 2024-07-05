package org.twins.core.mappers.rest.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinCommentEntity;
import org.twins.core.dto.rest.comment.CommentBaseDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class CommentRestDTOReversedMapper extends RestSimpleDTOMapper<CommentBaseDTOv1, TwinCommentEntity> {
    @Override
    public void map(CommentBaseDTOv1 src, TwinCommentEntity dst, MapperContext mapperContext) throws Exception {
                dst
                        .setText(src.getText())
                        .setTwinId(src.getTwinId());
    }
}
