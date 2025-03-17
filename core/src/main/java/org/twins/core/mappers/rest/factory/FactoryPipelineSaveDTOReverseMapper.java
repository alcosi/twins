package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineSaveDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineSaveDTOv1, TwinFactoryPipelineEntity> {
    @Override
    public void map(FactoryPipelineSaveDTOv1 src, TwinFactoryPipelineEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setOutputTwinStatusId(src.getOutputStatusId())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setNextTwinFactoryLimitScope(src.getNextTwinFactoryLimitScope())
                .setTemplateTwinId(src.getTemplateTwinId())
                .setDescription(src.getDescription());
    }
}
