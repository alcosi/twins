package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryPipelineStepCountDTOv1;
import org.twins.core.enums.sort.FactoryPipelineStepGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.service.factory.FactoryPipelineStepService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryPipelineMode.class, FactoryConditionSetMode.class, FeaturerMode.class})
public class FactoryPipelineStepCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryPipelineStepEntity, FactoryPipelineStepGroupField>, FactoryPipelineStepCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.class)
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryPipelineStep2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final FactoryPipelineStepService factoryPipelineStepService;

    @Override
    public void map(CountResult<TwinFactoryPipelineStepEntity, FactoryPipelineStepGroupField> src, FactoryPipelineStepCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryPipelineId(entity.getTwinFactoryPipelineId())
                .setFactoryConditionSetId(entity.getTwinFactoryConditionSetId())
                .setFillerFeaturerId(entity.getFillerFeaturerId())
                .setActive(entity.getActive())
                .setOptional(entity.getOptional())
                .setFactoryConditionInvert(entity.getTwinFactoryConditionInvert())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.HIDE, src, FactoryPipelineStepGroupField.factoryPipelineId)) {
            factoryPipelineStepService.loadPipeline(entity);
            factoryPipelineRestDTOMapper.convertOrPostpone(entity.getTwinFactoryPipeline(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.SHORT)));
        }
        if (needLoad(mapperContext, FeaturerMode.FactoryPipelineStep2FeaturerMode.HIDE, src, FactoryPipelineStepGroupField.fillerFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getFillerFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.FactoryPipelineStep2FeaturerMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.HIDE, src, FactoryPipelineStepGroupField.factoryConditionSetId)) {
            factoryPipelineStepService.loadConditionSet(entity);
            factoryConditionSetRestDTOMapper.convertOrPostpone(entity.getTwinFactoryConditionSet(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryPipelineStepEntity, FactoryPipelineStepGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryPipelineMode.FactoryPipelineStep2FactoryPipelineMode.HIDE, someCount, FactoryPipelineStepGroupField.factoryPipelineId)) {
            factoryPipelineStepService.loadPipeline(entityCollection);
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryPipelineStep2FactoryConditionSetMode.HIDE, someCount, FactoryPipelineStepGroupField.factoryConditionSetId)) {
            factoryPipelineStepService.loadConditionSet(entityCollection);
        }
    }
}
