package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryMultiplierSearch;
import org.twins.core.dto.rest.factory.FactoryMultiplierSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierSearchDTOv1, FactoryMultiplierSearch> {

    @Override
    public void map(FactoryMultiplierSearchDTOv1 src, FactoryMultiplierSearch dst, MapperContext mapperContext) {
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
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setActive(src.getActive());
    }
}
