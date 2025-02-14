package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineStepMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryPipelineStepMode.class)
public class FactoryPipelineStepRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryPipelineStepEntity, FactoryPipelineStepDTOv1> {

    @Override
    public void map(TwinFactoryPipelineStepEntity src, FactoryPipelineStepDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryPipelineStepMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setFactoryPipelineId(src.getTwinFactoryPipelineId())
                        .setOrder(src.getOrder())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionInvert(src.isTwinFactoryConditionInvert())
                        .setActive(src.isActive())
                        .setOptional(src.isOptional())
                        .setFillerFeaturerId(src.getFillerFeaturerId())
                        .setFillerParams(src.getFillerParams())
                        .setDescription(src.getDescription());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setFactoryPipelineId(src.getTwinFactoryPipelineId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setDescription(src.getDescription());
                break;
        }
    }
}
