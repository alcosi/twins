package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryConditionSearch;
import org.twins.core.dto.rest.factory.FactoryConditionSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryConditionSearchRqDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionSearchRqDTOv1, FactoryConditionSearch> {

    @Override
    public void map(FactoryConditionSearchRqDTOv1 src, FactoryConditionSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryConditionSetIdList(src.getFactoryConditionSetIdList())
                .setFactoryConditionSetIdExcludeList(src.getFactoryConditionSetIdExcludeList())
                .setConditionerFeaturerIdList(src.getConditionerFeaturerIdList())
                .setConditionerFeaturerIdExcludeList(src.getConditionerFeaturerIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setInvert(src.getInvert())
                .setActive(src.getActive());
    }
}
