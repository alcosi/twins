package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryTriggerCountDTOv1;
import org.twins.core.enums.sort.TwinFactoryTriggerGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryTriggerService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryTriggerMode.class)
public class FactoryTriggerCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryTriggerEntity, TwinFactoryTriggerGroupField>, FactoryTriggerCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;

    private final FactoryTriggerService factoryTriggerService;

    @Override
    public void map(CountResult<TwinFactoryTriggerEntity, TwinFactoryTriggerGroupField> src, FactoryTriggerCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinFactoryId(entity.getTwinFactoryId())
                .setInputTwinClassId(entity.getInputTwinClassId())
                .setTwinTriggerId(entity.getTwinTriggerId())
                .setActive(entity.getActive())
                .setAsync(entity.getAsync())
                .setTwinFactoryConditionInvert(entity.getTwinFactoryConditionInvert())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryTriggerMode.FactoryTrigger2FactoryMode.HIDE, src, TwinFactoryTriggerGroupField.twinFactoryId)) {
            factoryTriggerService.loadFactory(entity);
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryTriggerMode.FactoryTrigger2TwinClassMode.HIDE, src, TwinFactoryTriggerGroupField.inputTwinClassId)) {
            factoryTriggerService.loadClass(entity);
            twinClassRestDTOMapper.convertOrPostpone(entity.getTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.HIDE, src, TwinFactoryTriggerGroupField.twinTriggerId)) {
            factoryTriggerService.loadTwinTrigger(entity);
            twinTriggerRestDTOMapper.convertOrPostpone(entity.getTwinTrigger(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryTriggerEntity, TwinFactoryTriggerGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryTriggerMode.FactoryTrigger2FactoryMode.HIDE, sample, TwinFactoryTriggerGroupField.twinFactoryId)) {
            factoryTriggerService.loadFactories(entityCollection);
        }
        if (needLoad(mapperContext, FactoryTriggerMode.FactoryTrigger2TwinClassMode.HIDE, sample, TwinFactoryTriggerGroupField.inputTwinClassId)) {
            factoryTriggerService.loadClasses(entityCollection);
        }
        if (needLoad(mapperContext, FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.HIDE, sample, TwinFactoryTriggerGroupField.twinTriggerId)) {
            factoryTriggerService.loadTwinTriggers(entityCollection);
        }
    }
}
