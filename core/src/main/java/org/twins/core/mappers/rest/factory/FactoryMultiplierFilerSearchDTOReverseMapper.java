package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryMultiplierFilterSearch;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierFilerSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierFilterSearchRqDTOv1, FactoryMultiplierFilterSearch> {

    @Override
    public void map(FactoryMultiplierFilterSearchRqDTOv1 src, FactoryMultiplierFilterSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryIdList(src.getFactoryIdList())
                .setFactoryIdExcludeList(src.getFactoryIdExcludeList())
                .setFactoryMultiplierIdList(src.getFactoryMultiplierIdList())
                .setFactoryMultiplierIdExcludeList(src.getFactoryMultiplierIdExcludeList())
                .setInputTwinClassIdList(src.getInputTwinClassIdList())
                .setInputTwinClassIdExcludeList(src.getInputTwinClassIdExcludeList())
                .setFactoryConditionSetIdList(src.getFactoryConditionSetIdList())
                .setFactoryConditionSetIdExcludeList(src.getFactoryConditionSetIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setActive(src.getActive());
    }
}
