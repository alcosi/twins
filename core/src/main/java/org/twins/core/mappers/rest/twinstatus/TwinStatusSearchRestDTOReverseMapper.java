package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinStatusSearch;
import org.twins.core.dto.rest.twinstatus.TwinStatusSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twinclass.TwinClassIdsExtenderRestDTOReverseMapper;

@Component
@RequiredArgsConstructor
public class TwinStatusSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusSearchRqDTOv1, TwinStatusSearch> {
    private final TwinClassIdsExtenderRestDTOReverseMapper twinClassIdsExtenderRestDTOReverseMapper;

    @Override
    public void map(TwinStatusSearchRqDTOv1 src, TwinStatusSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassIdsExtenderList(twinClassIdsExtenderRestDTOReverseMapper.convertCollection(src.getTwinClassIdsExtenderList()))
                .setTwinClassIdsExtenderExcludeList(twinClassIdsExtenderRestDTOReverseMapper.convertCollection(src.getTwinClassIdsExtenderExcludeList()))
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameI18nLikeList(src.getNameI18nLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18nLikeList(src.getDescriptionI18nLikeList())
                .setDescriptionI18nNotLikeList(src.getDescriptionI18nNotLikeList());
    }
}
