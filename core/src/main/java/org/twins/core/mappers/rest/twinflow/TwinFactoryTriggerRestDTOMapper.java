package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinFactoryTriggerMode;
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
    @MapperModePointerBinding(modes = TwinFactoryTriggerMode.TwinFactoryTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    @MapperModePointerBinding(modes = TwinFactoryTriggerMode.TwinFactoryTrigger2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

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
            factoryRestDTOMapper.postpone(src.getTwinFactory(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinFactoryTriggerMode.TwinFactoryTrigger2FactoryMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2TwinTriggerMode.HIDE)) {
            twinTriggerRestDTOMapper.postpone(src.getTwinTrigger(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinFactoryTriggerMode.TwinFactoryTrigger2TwinTriggerMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2TwinClassMode.HIDE)) {
            twinClassRestDTOMapper.postpone(src.getTwinClass(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinFactoryTriggerMode.TwinFactoryTrigger2TwinClassMode.SHORT)));
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
        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2TwinTriggerMode.HIDE)) {
            factoryTriggerService.loadTriggers(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinFactoryTriggerMode.TwinFactoryTrigger2TwinClassMode.HIDE)) {
            factoryTriggerService.loadClasses(srcCollection);
        }
    }
}
