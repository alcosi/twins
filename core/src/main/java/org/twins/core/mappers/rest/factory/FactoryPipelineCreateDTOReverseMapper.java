package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryPipelineCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineCreateDTOv1, TwinFactoryPipelineEntity> {

    @Override
    public void map(FactoryPipelineCreateDTOv1 src, TwinFactoryPipelineEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinFactoryId(src.getFactoryId())
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
