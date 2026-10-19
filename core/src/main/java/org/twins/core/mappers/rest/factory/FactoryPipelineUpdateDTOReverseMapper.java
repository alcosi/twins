package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryPipelineUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineUpdateDTOv1, TwinFactoryPipelineEntity> {

    @Override
    public void map(FactoryPipelineUpdateDTOv1 src, TwinFactoryPipelineEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setOutputTwinStatusId(src.getOutputStatusId())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setTemplateTwinId(src.getTemplateTwinId())
                .setName(src.getName())
                .setDescription(src.getDescription());
    }
}
