package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineStepMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.service.factory.FactoryPipelineStepService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryPipelineStepMode.class)
public class FactoryPipelineStepRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryPipelineStepEntity, FactoryPipelineStepDTOv1> {

    @MapperModePointerBinding(modes = FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.class)
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryPipelineStep2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FactoryPipelineStepService factoryPipelineStepService;

    @Override
    public void map(TwinFactoryPipelineStepEntity src, FactoryPipelineStepDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryPipelineStepMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setFactoryPipelineId(src.getTwinFactoryPipelineId())
                        .setOrder(src.getOrder())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                        .setActive(src.getActive())
                        .setOptional(src.getOptional())
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
        if (mapperContext.hasModeButNot(FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.HIDE)) {
            dst.setFactoryPipelineId(src.getTwinFactoryPipelineId());
            factoryPipelineRestDTOMapper.postpone(src.getTwinFactoryPipeline(), mapperContext.forkOnPoint(FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.HIDE)) {
            dst.setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getTwinFactoryConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.FactoryPipelineStep2FeaturerMode.HIDE)) {
            dst.setFillerFeaturerId(src.getFillerFeaturerId());
            factoryPipelineStepService.loadFiller(src);
            featurerRestDTOMapper.postpone(src.getFillerFeaturer(), mapperContext.forkOnPoint(FeaturerMode.FactoryPipelineStep2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryPipelineStepEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FeaturerMode.FactoryPipelineStep2FeaturerMode.HIDE)) {
            factoryPipelineStepService.loadFillers(srcCollection);
        }
    }
}
