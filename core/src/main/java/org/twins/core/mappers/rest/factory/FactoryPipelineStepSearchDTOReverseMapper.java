package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryPipelineStepSearch;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineStepSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineStepSearchDTOv1, FactoryPipelineStepSearch> {

    @Override
    public void map(FactoryPipelineStepSearchDTOv1 src, FactoryPipelineStepSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryIdList(src.getFactoryIdList())
                .setFactoryIdExcludeList(src.getFactoryIdExcludeList())
                .setFactoryPipelineIdList(src.getFactoryPipelineIdList())
                .setFactoryPipelineIdExcludeList(src.getFactoryPipelineIdExcludeList())
                .setFactoryConditionSetIdList(src.getFactoryConditionSetIdList())
                .setFactoryConditionSetIdExcludeList(src.getFactoryConditionSetIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setFillerFeaturerIdList(src.getFillerFeaturerIdList())
                .setFillerFeaturerIdExcludeList(src.getFillerFeaturerIdExcludeList())
                .setConditionInvert(src.getConditionInvert())
                .setActive(src.getActive())
                .setOptional(src.getOptional());
    }
}
