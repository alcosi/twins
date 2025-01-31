package org.twins.core.mappers.rest.comment;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.dto.rest.comment.CommentSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class CommentSearchRestDTOMapper extends RestSimpleDTOMapper<CommentSearchRqDTOv1, CommentSearch> {
    @Override
    public void map(CommentSearchRqDTOv1 src, CommentSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinIdList(src.getTwinIdList())
                .setTwinIdExcludeList(src.getTwinIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setTextLikeList(src.getTextLikeList())
                .setTextNotLikeList(src.getTextNotLikeList())
                .setCreatedAt(src.getCreatedAt())
                .setUpdatedAt(src.getUpdatedAt());
    }

}
