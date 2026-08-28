package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.factory.FactoryTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryTriggerMode.class)
public class FactoryTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryTriggerEntity, FactoryTriggerDTOv1> {
    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    @MapperModePointerBinding(modes = FactoryTriggerMode.FactoryTrigger2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    private final FactoryTriggerService factoryTriggerService;

    @Override
    public void map(TwinFactoryTriggerEntity src, FactoryTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                    .setActive(src.getActive())
                    .setDescription(src.getDescription())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync());
            case SHORT -> dst
                    .setId(src.getId())
                    .setFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }

        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2FactoryMode.HIDE)) {
            factoryTriggerService.loadFactory(src);
            dst.setFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getTwinFactory(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2FactoryMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.HIDE)) {
            factoryTriggerService.loadTwinTrigger(src);
            dst.setTwinTriggerId(src.getTwinTriggerId());
            twinTriggerRestDTOMapper.postpone(src.getTwinTrigger(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2TwinClassMode.HIDE)) {
            factoryTriggerService.loadClass(src);
            twinClassRestDTOMapper.postpone(src.getTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2FactoryConditionSetMode.HIDE)) {
            factoryTriggerService.loadConditionSet(src);
            dst.setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getTwinFactoryConditionSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryTriggerMode.FactoryTrigger2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(FactoryTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinFactoryTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2FactoryMode.HIDE)) {
            factoryTriggerService.loadFactories(srcCollection);
        }
        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2TwinClassMode.HIDE)) {
            factoryTriggerService.loadClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2FactoryConditionSetMode.HIDE)) {
            factoryTriggerService.loadConditionSets(srcCollection);
        }
        if (mapperContext.hasModeButNot(FactoryTriggerMode.FactoryTrigger2TwinTriggerMode.HIDE)) {
            factoryTriggerService.loadTwinTriggers(srcCollection);
        }
    }
}
