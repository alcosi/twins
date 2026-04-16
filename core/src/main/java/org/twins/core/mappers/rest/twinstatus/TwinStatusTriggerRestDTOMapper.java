package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinStatusTriggerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.service.twin.TwinStatusTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinStatusTriggerMode.class)
public class TwinStatusTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinStatusTriggerEntity, TwinStatusTriggerDTOv1> {
    @MapperModePointerBinding(modes = TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    @MapperModePointerBinding(modes = StatusMode.TwinStatusTrigger2TwinStatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final TwinStatusTriggerService twinStatusTriggerService;

    @Override
    public void map(TwinStatusTriggerEntity src, TwinStatusTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinStatusTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinStatusId(src.getTwinStatusId())
                    .setIncomingElseOutgoing(src.getIncomingElseOutgoing())
                    .setOrder(src.getOrder())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync())
                    .setActive(src.getActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinStatusId(src.getTwinStatusId())
                    .setIncomingElseOutgoing(src.getIncomingElseOutgoing())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }

        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.HIDE)) {
            twinStatusTriggerService.loadTrigger(src);
            dst.setTwinTriggerId(src.getTwinTriggerId());
            twinTriggerRestDTOMapper.postpone(src.getTwinTrigger(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(StatusMode.TwinStatusTrigger2TwinStatusMode.HIDE)) {
            twinStatusTriggerService.loadStatus(src);
            dst.setTwinStatusId(src.getTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getTwinStatus(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinStatusTrigger2TwinStatusMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinStatusTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinStatusTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinStatusTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.HIDE)) {
            twinStatusTriggerService.loadTriggers(srcCollection);
        }
        if (mapperContext.hasModeButNot(StatusMode.TwinStatusTrigger2TwinStatusMode.HIDE)) {
            twinStatusTriggerService.loadStatuses(srcCollection);
        }
    }
}
