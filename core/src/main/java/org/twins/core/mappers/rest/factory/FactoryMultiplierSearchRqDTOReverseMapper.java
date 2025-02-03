package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryConditionSetSearch;
import org.twins.core.domain.search.FactoryMultiplierSearch;
import org.twins.core.dto.rest.factory.FactoryConditionSetSearchRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierSearchRqDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierSearchRqDTOv1, FactoryMultiplierSearch> {

    @Override
    public void map(FactoryMultiplierSearchRqDTOv1 src, FactoryMultiplierSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryIdList(src.getFactoryIdList())
                .setFactoryIdExcludeList(src.getFactoryIdExcludeList())
                .setInputTwinClassIdList(src.getInputTwinClassIdList())
                .setInputTwinClassIdExcludeList(src.getInputTwinClassIdExcludeList())
                .setMultiplierFeaturerIdList(src.getMultiplierFeaturerIdList())
                .setMultiplierFeaturerIdExcludeList(src.getMultiplierFeaturerIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionLikeList(src.getDescriptionNotLikeList())
                .setActive(src.getActive());
    }
}
