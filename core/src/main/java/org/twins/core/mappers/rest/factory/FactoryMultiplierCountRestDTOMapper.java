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
import org.twins.core.service.factory.FactoryMultiplierService;

import java.util.Collection;
import java.util.Objects;

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

    private final FactoryMultiplierService factoryMultiplierService;

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
            factoryMultiplierService.loadTwinFactory(entity);
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryMultiplier2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryMultiplier2TwinClassMode.HIDE, src, FactoryMultiplierGroupField.inputTwinClassId)) {
            factoryMultiplierService.loadInputTwinClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getInputTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.FactoryMultiplier2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, FeaturerMode.FactoryMultiplier2FeaturerMode.HIDE, src, FactoryMultiplierGroupField.multiplierFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getMultiplierFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.FactoryMultiplier2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryMultiplierEntity, FactoryMultiplierGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryMode.FactoryMultiplier2FactoryMode.HIDE, someCount, FactoryMultiplierGroupField.factoryId)) {
            factoryMultiplierService.loadTwinFactory(entityCollection);
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryMultiplier2TwinClassMode.HIDE, someCount, FactoryMultiplierGroupField.inputTwinClassId)) {
            factoryMultiplierService.loadInputTwinClass(entityCollection);
        }
    }
}
