package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinFactoryTriggerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinFactoryTriggerMode.class)
public class TwinFactoryTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryTriggerEntity, TwinFactoryTriggerDTOv1> {
    @MapperModePointerBinding(modes = TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    @MapperModePointerBinding(modes = TwinTriggerMode.TwinFactoryTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    @MapperModePointerBinding(modes = TwinClassMode.TwinFactoryTrigger2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    @MapperModePointerBinding(modes = TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    private final FactoryTriggerService factoryTriggerService;

    @Override
    public void map(TwinFactoryTriggerEntity src, TwinFactoryTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinFactoryTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                    .setActive(src.getActive())
                    .setDescription(src.getDescription())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }

        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryMode.HIDE)) {
            factoryTriggerService.loadFactory(src);
            dst.setTwinFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getTwinFactory(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinFactoryTrigger2TwinTriggerMode.HIDE)) {
            twinTriggerRestDTOMapper.postpone(src.getTwinTrigger(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TwinFactoryTrigger2TwinTriggerMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.TwinFactoryTrigger2TwinClassMode.HIDE)) {
            twinClassRestDTOMapper.postpone(src.getTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinFactoryTrigger2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryConditionSetMode.HIDE)) {
            factoryTriggerService.loadConditionSet(src);
            dst.setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getTwinFactoryConditionSet(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinFactoryTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinFactoryTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryMode.HIDE)) {
            factoryTriggerService.loadFactories(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.TwinFactoryTrigger2TwinClassMode.HIDE)) {
            factoryTriggerService.loadClasses(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryConditionSetMode.HIDE)) {
            factoryTriggerService.loadConditionSets(srcCollection);
        }
    }
}
