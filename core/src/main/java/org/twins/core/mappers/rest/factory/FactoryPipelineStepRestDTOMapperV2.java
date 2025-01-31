package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.permission.FactoryPipelineStepDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;

@Component
@RequiredArgsConstructor
public class FactoryPipelineStepRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryPipelineStepEntity, FactoryPipelineStepDTOv2> {

    private final FactoryPipelineStepRestDTOMapper factoryPipelineStepRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.class)
    private final FactoryPipelineRestDTOMapperV2 factoryPipelineRestDTOMapperV2;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryPipelineStep2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(TwinFactoryPipelineStepEntity src, FactoryPipelineStepDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryPipelineStepRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.HIDE))
            dst
                    .setFactoryPipeline(factoryPipelineRestDTOMapperV2.convertOrPostpone(src.getTwinFactoryPipeline(), mapperContext.forkOnPoint(FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.SHORT)))
                    .setFactoryPipelineId(src.getTwinFactoryPipelineId());
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.HIDE))
            dst
                    .setFactoryConditionSet(factoryConditionSetRestDTOMapper.convertOrPostpone(src.getTwinFactoryConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.SHORT)))
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
        if (mapperContext.hasModeButNot(FeaturerMode.FactoryPipelineStep2FeaturerMode.HIDE))
            dst
                    .setFillerFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getFillerFeaturer(), mapperContext.forkOnPoint(FeaturerMode.FactoryPipelineStep2FeaturerMode.SHORT)))
                    .setFillerFeaturerId(src.getFillerFeaturerId());
    }
}
