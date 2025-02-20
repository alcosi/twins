package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TransitionAliasSearch;
import org.twins.core.dto.rest.transition.TransitionAliasSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TransitionAliasSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionAliasSearchRqDTOv1, TransitionAliasSearch> {

    @Override
    public void map(TransitionAliasSearchRqDTOv1 src, TransitionAliasSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setAliasLikeList(src.getAliasLikeList())
                .setAliasNotLikeList(src.getAliasNotLikeList());
    }
}
