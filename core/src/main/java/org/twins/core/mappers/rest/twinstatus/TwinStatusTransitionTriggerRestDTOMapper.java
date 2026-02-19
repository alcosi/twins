package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinStatusTransitionTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.service.twin.TwinStatusTransitionTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinStatusTransitionTriggerMode.class)
public class TwinStatusTransitionTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinStatusTransitionTriggerEntity, TwinStatusTransitionTriggerDTOv1> {
    @MapperModePointerBinding(modes = TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    @MapperModePointerBinding(modes = TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinStatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final TwinStatusTransitionTriggerService twinStatusTransitionTriggerService;

    @Override
    public void map(TwinStatusTransitionTriggerEntity src, TwinStatusTransitionTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinStatusTransitionTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinStatusId(src.getTwinStatusId())
                    .setType(src.getType())
                    .setOrder(src.getOrder())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync())
                    .setActive(src.getActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinStatusId(src.getTwinStatusId())
                    .setType(src.getType())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }

        if (mapperContext.hasModeButNot(TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinTriggerMode.HIDE)) {
            twinTriggerRestDTOMapper.postpone(src.getTwinTrigger(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinTriggerMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinStatusMode.HIDE)) {
            twinStatusRestDTOMapper.postpone(src.getTwinStatus(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinStatusMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinStatusTransitionTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinStatusTransitionTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinStatusTransitionTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinTriggerMode.HIDE)) {
            twinStatusTransitionTriggerService.loadTriggers(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinStatusTransitionTriggerMode.TwinStatusTransitionTrigger2TwinStatusMode.HIDE)) {
            twinStatusTransitionTriggerService.loadStatuses(srcCollection);
        }
    }
}
