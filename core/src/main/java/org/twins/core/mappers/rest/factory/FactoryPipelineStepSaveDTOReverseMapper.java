package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineStepSaveDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineStepSaveDTOv1, TwinFactoryPipelineStepEntity>{
    @Override
    public void map(FactoryPipelineStepSaveDTOv1 src, TwinFactoryPipelineStepEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinFactoryPipelineId(src.getFactoryPipelineId())
                .setOrder(src.getOrder())
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setFillerFeaturerId(src.getFillerFeaturerId())
                .setFillerParams(src.getFillerParams())
                .setDescription(src.getDescription())
                .setOptional(src.getOptional());
    }
}
