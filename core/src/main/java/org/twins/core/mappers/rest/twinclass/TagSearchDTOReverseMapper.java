package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.dto.rest.twinclass.TagSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Set;


@Component
@RequiredArgsConstructor
public class TagSearchDTOReverseMapper extends RestSimpleDTOMapper<TagSearchRqDTOv1, DataListOptionSearch> {
    private final TwinClassService twinClassService;

    @Override
    public void map(TagSearchRqDTOv1 src, DataListOptionSearch dst, MapperContext mapperContext) throws Exception {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(src.getTwinClassId());
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setOptionLikeList(src.getOptionLikeList())
                .setOptionNotLikeList(src.getOptionNotLikeList())
                .setOptionI18nLikeList(src.getOptionI18nLikeList())
                .setOptionI18nNotLikeList(src.getOptionI18nNotLikeList())
                .setDataListIdList(Set.of(twinClassEntity.getTagDataListId()))
        ;
    }
}