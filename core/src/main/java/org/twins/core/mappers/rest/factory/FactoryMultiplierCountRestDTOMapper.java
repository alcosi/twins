package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryMultiplierCountDTOv1;
import org.twins.core.enums.sort.FactoryMultiplierGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMode.class, TwinClassMode.class, FeaturerMode.class})
public class FactoryMultiplierCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryMultiplierEntity, FactoryMultiplierGroupField>, FactoryMultiplierCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryMultiplier2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryMultiplier2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryMultiplier2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(CountResult<TwinFactoryMultiplierEntity, FactoryMultiplierGroupField> src, FactoryMultiplierCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryId(entity.getTwinFactoryId())
                .setInputTwinClassId(entity.getInputTwinClassId())
                .setMultiplierFeaturerId(entity.getMultiplierFeaturerId())
                .setActive(entity.getActive())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryMode.FactoryMultiplier2FactoryMode.HIDE, src, FactoryMultiplierGroupField.factoryId)) {
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryMultiplier2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryMultiplier2TwinClassMode.HIDE, src, FactoryMultiplierGroupField.inputTwinClassId)) {
            twinClassRestDTOMapper.convertOrPostpone(entity.getInputTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.FactoryMultiplier2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, FeaturerMode.FactoryMultiplier2FeaturerMode.HIDE, src, FactoryMultiplierGroupField.multiplierFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getMultiplierFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.FactoryMultiplier2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryMultiplierEntity, FactoryMultiplierGroupField>> srcCollection, MapperContext mapperContext) {
        // No batch-load for related objects in factory_multiplier count (loaded lazily per entity)
    }
}
