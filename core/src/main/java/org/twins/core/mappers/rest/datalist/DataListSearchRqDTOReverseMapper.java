package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListSearch;
import org.twins.core.dto.rest.datalist.DataListSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class DataListSearchRqDTOReverseMapper extends RestSimpleDTOMapper<DataListSearchRqDTOv1, DataListSearch> {
    @Override
    public void map(DataListSearchRqDTOv1 src, DataListSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setOptionLikeList(src.getOptionLikeList())
                .setOptionNotLikeList(src.getOptionNotLikeList())
                .setOptionI18nLikeList(src.getOptionI18nLikeList())
                .setOptionI18nNotLikeList(src.getOptionI18nNotLikeList());
    }
}
