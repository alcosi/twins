package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.dto.rest.twinclass.TagSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TagSearchDTOReverseMapper extends RestSimpleDTOMapper<TagSearchRqDTOv1, DataListOptionSearch> {

    @Override
    public void map(TagSearchRqDTOv1 src, DataListOptionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setOptionLikeList(src.getOptionLikeList())
                .setOptionNotLikeList(src.getOptionNotLikeList())
                .setOptionI18nLikeList(src.getOptionI18nLikeList())
                .setOptionI18nNotLikeList(src.getOptionI18nNotLikeList())
        ;
    }
}