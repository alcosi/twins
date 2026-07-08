package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryPipelineCountDTOv1;
import org.twins.core.enums.sort.FactoryPipelineGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.factory.FactoryPipelineService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMode.class, TwinClassMode.class, FactoryConditionSetMode.class, StatusMode.class})
public class FactoryPipelineCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryPipelineEntity, FactoryPipelineGroupField>, FactoryPipelineCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryPipeline2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryPipeline2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final FactoryPipelineService factoryPipelineService;

    @Override
    public void map(CountResult<TwinFactoryPipelineEntity, FactoryPipelineGroupField> src, FactoryPipelineCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryId(entity.getTwinFactoryId())
                .setInputTwinClassId(entity.getInputTwinClassId())
                .setFactoryConditionSetId(entity.getTwinFactoryConditionSetId())
                .setOutputTwinStatusId(entity.getOutputTwinStatusId())
                .setNextFactoryId(entity.getNextTwinFactoryId())
                .setActive(entity.getActive())
                .setNextFactoryLimitScope(entity.getNextTwinFactoryLimitScope())
                .setFactoryConditionSetInvert(entity.getTwinFactoryConditionInvert())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryMode.FactoryPipeline2FactoryMode.HIDE, src, FactoryPipelineGroupField.factoryId)) {
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryPipeline2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.HIDE, src, FactoryPipelineGroupField.nextFactoryId)) {
            factoryRestDTOMapper.convertOrPostpone(entity.getNextTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryPipeline2TwinClassMode.HIDE, src, FactoryPipelineGroupField.inputTwinClassId)) {
            twinClassRestDTOMapper.convertOrPostpone(entity.getInputTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.FactoryPipeline2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.HIDE, src, FactoryPipelineGroupField.outputTwinStatusId)) {
            twinStatusRestDTOMapper.convertOrPostpone(entity.getOutputTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.HIDE, src, FactoryPipelineGroupField.factoryConditionSetId)) {
            factoryPipelineService.loadConditionSet(entity);
            factoryConditionSetRestDTOMapper.convertOrPostpone(entity.getConditionSet(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryPipelineEntity, FactoryPipelineGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.HIDE, someCount, FactoryPipelineGroupField.factoryConditionSetId)) {
            factoryPipelineService.loadConditionSet(entityCollection);
        }
    }
}
