package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierFilterRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterDTOv2> {

    private final FactoryMultiplierFilterRestDTOMapper factoryMultiplierFilterRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.class)
    private final FactoryMultiplierRestDTOMapperV2 factoryMultiplierRestDTOMapperV2;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryMultiplierFilter2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @Override
    public void map(TwinFactoryMultiplierFilterEntity src, FactoryMultiplierFilterDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryMultiplierFilterRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.HIDE))
            dst
                    .setMultiplier(factoryMultiplierRestDTOMapperV2.convertOrPostpone(src.getMultiplier(), mapperContext.forkOnPoint(FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.SHORT)))
                    .setMultiplierId(src.getTwinFactoryMultiplierId());
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.HIDE))
            dst
                    .setFactoryConditionSet(factoryConditionSetRestDTOMapper.convertOrPostpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.SHORT)))
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryMultiplier2TwinClassMode.HIDE))
            dst
                    .setInputTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryMultiplierFilter2TwinClassMode.SHORT)))
                    .setInputTwinClassId(src.getInputTwinClassId());
    }
}
