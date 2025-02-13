package org.twins.core.mappers.rest.comment;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.dto.rest.comment.CommentSearchRqDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@AllArgsConstructor
public class CommentSearchRestDTOMapper extends RestSimpleDTOMapper<CommentSearchRqDTOv1, CommentSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(CommentSearchRqDTOv1 src, CommentSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinIdList(src.getTwinIdList())
                .setTwinIdExcludeList(src.getTwinIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setTextLikeList(src.getTextLikeList())
                .setTextNotLikeList(src.getTextNotLikeList())
                .setCreatedAt(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()))
                .setUpdatedAt(dataTimeRangeDTOReverseMapper.convert(src.getUpdatedAt()));
    }

}
