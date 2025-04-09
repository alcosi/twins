package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinflowSearch;
import org.twins.core.dto.rest.twinflow.TwinflowSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowSearchRqDTOv1, TwinflowSearch> {

    @Override
    public void map(TwinflowSearchRqDTOv1 src, TwinflowSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassIdMap(src.getTwinClassIdMap())
                .setTwinClassIdExcludeMap(src.getTwinClassIdExcludeMap())
                .setNameI18nLikeList(src.getNameI18nLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18nLikeList(src.getDescriptionI18nLikeList())
                .setDescriptionI18nNotLikeList(src.getDescriptionI18nNotLikeList())
                .setInitialStatusIdList(src.getInitialStatusIdList())
                .setInitialStatusIdExcludeList(src.getInitialStatusIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setTwinflowSchemaIdList(src.getTwinflowSchemaIdList())
                .setTwinflowSchemaIdExcludeList(src.getTwinflowSchemaIdExcludeList());
    }
}
