package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterCountDTOv1;
import org.twins.core.enums.sort.FactoryMultiplierFilterGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryMultiplierFilterService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMultiplierMode.class, TwinClassMode.class, FactoryConditionSetMode.class})
public class FactoryMultiplierFilterCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterGroupField>, FactoryMultiplierFilterCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.class)
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryMultiplierFilter2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    private final FactoryMultiplierFilterService factoryMultiplierFilterService;

    @Override
    public void map(CountResult<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterGroupField> src, FactoryMultiplierFilterCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryMultiplierId(entity.getTwinFactoryMultiplierId())
                .setInputTwinClassId(entity.getInputTwinClassId())
                .setFactoryConditionSetId(entity.getTwinFactoryConditionSetId())
                .setActive(entity.isActive())
                .setFactoryConditionSetInvert(entity.isTwinFactoryConditionInvert())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.HIDE, src, FactoryMultiplierFilterGroupField.factoryMultiplierId)) {
            factoryMultiplierFilterService.loadMultiplier(src.getEntity());
            factoryMultiplierRestDTOMapper.convertOrPostpone(entity.getMultiplier(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryMultiplierFilter2TwinClassMode.HIDE, src, FactoryMultiplierFilterGroupField.inputTwinClassId)) {
            factoryMultiplierFilterService.loadInputTwinClass(src.getEntity());
            twinClassRestDTOMapper.convertOrPostpone(entity.getInputTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.FactoryMultiplierFilter2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.HIDE, src, FactoryMultiplierFilterGroupField.factoryConditionSetId)) {
            factoryMultiplierFilterService.loadConditionSet(src.getEntity());
            factoryConditionSetRestDTOMapper.convertOrPostpone(entity.getConditionSet(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.HIDE, someCount, FactoryMultiplierFilterGroupField.factoryMultiplierId)) {
            factoryMultiplierFilterService.loadMultiplier(entityCollection);
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryMultiplierFilter2TwinClassMode.HIDE, someCount, FactoryMultiplierFilterGroupField.inputTwinClassId)) {
            factoryMultiplierFilterService.loadInputTwinClass(entityCollection);
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.HIDE, someCount, FactoryMultiplierFilterGroupField.factoryConditionSetId)) {
            factoryMultiplierFilterService.loadConditionSet(entityCollection);
        }
    }
}
