package org.twins.core.mappers.rest.comment;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.CommentSearch;
import org.twins.core.dto.rest.comment.CommentSearchDTO;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@AllArgsConstructor
public class CommentSearchDTORestDTOMapper extends RestSimpleDTOMapper<CommentSearchDTO, CommentSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(CommentSearchDTO src, CommentSearch dst, MapperContext mapperContext) throws Exception {
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
