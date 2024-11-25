package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinStatusSearch;
import org.twins.core.dto.rest.twinstatus.TwinStatusSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinStatusSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusSearchRqDTOv1, TwinStatusSearch> {
    @Override
    public void map(TwinStatusSearchRqDTOv1 src, TwinStatusSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setTwinClassIdList(src.getTwinClassIdList())
                .setTwinClassIdExcludeList(src.getTwinClassIdExcludeList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameI18nLikeList(src.getNameI18nLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18nLikeList(src.getDescriptionI18nLikeList())
                .setDescriptionI18nNotLikeList(src.getDescriptionI18nNotLikeList());
    }
}
