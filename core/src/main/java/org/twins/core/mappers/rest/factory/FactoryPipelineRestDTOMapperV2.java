package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FactoryPipelineRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryPipelineEntity, FactoryPipelineDTOv2> {

    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;

    @MapperModePointerBinding(modes = {
            FactoryMode.FactoryPipeline2FactoryMode.class,
            FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.class})
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryPipeline2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @Override
    public void map(TwinFactoryPipelineEntity src, FactoryPipelineDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryPipelineRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(FactoryMode.FactoryPipeline2FactoryMode.HIDE))
            dst
                    .setFactory(factoryRestDTOMapper.convertOrPostpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryPipeline2FactoryMode.SHORT)))
                    .setFactoryId(src.getTwinFactoryId());
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryPipeline2TwinClassMode.HIDE))
            dst
                    .setInputTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryPipeline2TwinClassMode.SHORT)))
                    .setInputTwinClassId(src.getInputTwinClassId());
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.HIDE))
            dst
                    .setFactoryConditionSet(factoryConditionSetRestDTOMapper.convertOrPostpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryPipeline2FactoryConditionSetMode.SHORT)))
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
        if (mapperContext.hasModeButNot(StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.HIDE))
            dst
                    .setOutputTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getOutputTwinStatus(), mapperContext.forkOnPoint(StatusMode.FactoryPipelineOutputTwinStatus2StatusMode.SHORT)))
                    .setOutputTwinStatusId(src.getOutputTwinStatusId());
        if (mapperContext.hasModeButNot(FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.HIDE))
            dst
                    .setNextFactory(factoryRestDTOMapper.convertOrPostpone(src.getNextTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryPipelineNextTwinFactory2FactoryMode.SHORT)))
                    .setNextFactoryId(src.getNextTwinFactoryId());
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryPipelineEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        factoryPipelineRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
    }
}
