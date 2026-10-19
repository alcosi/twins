package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerCountDTOv1;
import org.twins.core.enums.sort.TwinStatusTriggerGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinStatusTriggerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.service.twinstatus.TwinStatusTriggerService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinStatusTriggerMode.class)
public class TwinStatusTriggerCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinStatusTriggerEntity, TwinStatusTriggerGroupField>, TwinStatusTriggerCountDTOv1> {

    @MapperModePointerBinding(modes = StatusMode.TwinStatusTrigger2TwinStatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;

    private final TwinStatusTriggerService twinStatusTriggerService;

    @Override
    public void map(CountResult<TwinStatusTriggerEntity, TwinStatusTriggerGroupField> src, TwinStatusTriggerCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinStatusId(entity.getTwinStatusId())
                .setTwinTriggerId(entity.getTwinTriggerId())
                .setActive(entity.getActive())
                .setAsync(entity.getAsync())
                .setIncomingElseOutgoing(entity.getIncomingElseOutgoing())
                .setCount(src.getCount());
        if (needLoad(mapperContext, StatusMode.TwinStatusTrigger2TwinStatusMode.HIDE, src, TwinStatusTriggerGroupField.twinStatusId)) {
            twinStatusTriggerService.loadStatus(entity);
            twinStatusRestDTOMapper.convertOrPostpone(entity.getTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinStatusTrigger2TwinStatusMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.HIDE, src, TwinStatusTriggerGroupField.twinTriggerId)) {
            twinStatusTriggerService.loadTrigger(entity);
            twinTriggerRestDTOMapper.convertOrPostpone(entity.getTwinTrigger(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinStatusTriggerEntity, TwinStatusTriggerGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, StatusMode.TwinStatusTrigger2TwinStatusMode.HIDE, sample, TwinStatusTriggerGroupField.twinStatusId)) {
            twinStatusTriggerService.loadStatuses(entityCollection);
        }
        if (needLoad(mapperContext, TwinTriggerMode.TwinStatusTrigger2TwinTriggerMode.HIDE, sample, TwinStatusTriggerGroupField.twinTriggerId)) {
            twinStatusTriggerService.loadTriggers(entityCollection);
        }
    }
}
