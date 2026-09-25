package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryPipelineUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineUpdateRqDTOv1, TwinFactoryPipelineEntity> {

    @Override
    public void map(FactoryPipelineUpdateRqDTOv1 src, TwinFactoryPipelineEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getFactoryPipeline().getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getFactoryPipeline().getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryPipeline().getFactoryConditionSetInvert())
                .setActive(src.getFactoryPipeline().getActive())
                .setOutputTwinStatusId(src.getFactoryPipeline().getOutputStatusId())
                .setNextTwinFactoryId(src.getFactoryPipeline().getNextFactoryId())
                .setTemplateTwinId(src.getFactoryPipeline().getTemplateTwinId())
                .setDescription(src.getFactoryPipeline().getDescription());
    }
}
