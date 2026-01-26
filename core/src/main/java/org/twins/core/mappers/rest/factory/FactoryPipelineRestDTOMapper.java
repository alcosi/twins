package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryPipelineMode.class)
public class FactoryPipelineRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryPipelineEntity, FactoryPipelineDTOv1> {

    private final TwinFactoryService twinFactoryService;

    @MapperModePointerBinding(modes = {
            FactoryMode.FactoryPipeline2FactoryMode.class,
            FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.class})
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryPipeline2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @Override
    public void map(TwinFactoryPipelineEntity src, FactoryPipelineDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryPipelineMode.DETAILED)) {
            case DETAILED:
                twinFactoryService.countFactoryPipelineSteps(src);
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionSetInvert(src.getTwinFactoryConditionInvert())
                        .setActive(src.getActive())
                        .setOutputTwinStatusId(src.getOutputTwinStatusId())
                        .setNextFactoryId(src.getNextTwinFactoryId())
                        .setNextFactoryLimitScope(src.getNextTwinFactoryLimitScope())
                        .setPipelineStepsCount(src.getPipelineStepsCount())
                        .setDescription(src.getDescription());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId());
                break;
        }
        if (mapperContext.hasModeButNot(FactoryMode.FactoryPipeline2FactoryMode.HIDE)) {
            dst.setFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryPipeline2FactoryMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryPipeline2TwinClassMode.HIDE)) {
            dst.setInputTwinClassId(src.getInputTwinClassId());
            twinClassRestDTOMapper.postpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryPipeline2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.HIDE)) {
            dst.setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.SHORT));
        }
        if (mapperContext.hasModeButNot(StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.HIDE)) {
            dst.setOutputTwinStatusId(src.getOutputTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getOutputTwinStatus(), mapperContext.forkOnPoint(StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.HIDE)) {
            dst.setNextFactoryId(src.getNextTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getNextTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryPipelineEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(FactoryPipelineMode.DETAILED)) {
            twinFactoryService.countFactoryPipelineSteps(srcCollection);
        }
    }
}
