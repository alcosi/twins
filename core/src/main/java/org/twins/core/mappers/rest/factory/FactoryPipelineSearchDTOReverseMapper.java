package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryPipelineSearch;
import org.twins.core.dto.rest.factory.FactoryPipelineSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineSearchRqDTOv1, FactoryPipelineSearch> {

    @Override
    public void map(FactoryPipelineSearchRqDTOv1 src, FactoryPipelineSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryIdList(src.getFactoryIdList())
                .setFactoryIdExcludeList(src.getFactoryIdExcludeList())
                .setInputTwinClassIdList(src.getInputTwinClassIdList())
                .setInputTwinClassIdExcludeList(src.getInputTwinClassIdExcludeList())
                .setFactoryConditionSetIdList(src.getFactoryConditionSetIdList())
                .setFactoryConditionSetIdExcludeList(src.getFactoryConditionSetIdExcludeList())
                .setOutputTwinStatusIdList(src.getOutputTwinStatusIdList())
                .setOutputTwinStatusIdExcludeList(src.getOutputTwinStatusIdExcludeList())
                .setNextFactoryIdList(src.getNextFactoryIdList())
                .setNextFactoryIdExcludeList(src.getNextFactoryIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setActive(src.getActive())
                .setNextFactoryLimitScope(src.getNextFactoryLimitScope());
    }
}
