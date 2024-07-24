package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinflowSearch;
import org.twins.core.dto.rest.twinflow.TwinflowSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinflowSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowSearchRqDTOv1, TwinflowSearch> {

    @Override
    public void map(TwinflowSearchRqDTOv1 src, TwinflowSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertToSetSafe(src.getTwinClassIdExcludeList()))
                .setNameI18nLikeList(convertToSetSafe(src.getNameI18nLikeList()))
                .setNameI18nNotLikeList(convertToSetSafe(src.getNameI18nNotLikeList()))
                .setDescriptionI18nLikeList(convertToSetSafe(src.getDescriptionI18nLikeList()))
                .setDescriptionI18nNotLikeList(convertToSetSafe(src.getDescriptionI18nNotLikeList()))
                .setInitialStatusIdList(convertToSetSafe(src.getInitialStatusIdList()))
                .setInitialStatusIdExcludeList(convertToSetSafe(src.getInitialStatusIdExcludeList()));
    }
}
