package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierDTOv2;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.permission.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryMultiplierEntity, FactoryMultiplierDTOv2> {

    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.FactoryMultiplierTwinFactory2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryMultiplier2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @Override
    public void map(TwinFactoryMultiplierEntity src, FactoryMultiplierDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryMultiplierRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(FactoryMode.FactoryMultiplierTwinFactory2FactoryMode.HIDE))
            dst
                    .setFactory(factoryRestDTOMapper.convertOrPostpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryMultiplierTwinFactory2FactoryMode.SHORT)))
                    .setFactoryId(src.getTwinFactoryId());
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryMultiplier2TwinClassMode.HIDE))
            dst
                    .setInputTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryMultiplier2TwinClassMode.SHORT)))
                    .setInputTwinClassId(src.getInputTwinClassId());

    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryMultiplierEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        factoryMultiplierRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
    }
}
