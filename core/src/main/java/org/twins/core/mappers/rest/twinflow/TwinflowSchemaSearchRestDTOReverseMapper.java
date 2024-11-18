package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinflowSchemaSearch;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinflowSchemaSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowSchemaSearchRqDTOv1, TwinflowSchemaSearch> {

    @Override
    public void map(TwinflowSchemaSearchRqDTOv1 src, TwinflowSchemaSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setBusinessAccountIdList(src.getBusinessAccountIdList())
                .setBusinessAccountIdExcludeList(src.getBusinessAccountIdExcludeList())
                .setCreatedByUserIdList(convertToSetSafe(src.getCreatedByUserIdList()))
                .setCreatedByUserIdExcludeList(convertToSetSafe(src.getCreatedByUserIdExcludeList()));
    }
}
