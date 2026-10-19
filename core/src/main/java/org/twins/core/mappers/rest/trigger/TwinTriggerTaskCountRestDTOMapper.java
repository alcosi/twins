package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskCountDTOv1;
import org.twins.core.enums.sort.TwinTriggerTaskGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twintrigger.TwinTriggerTaskService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinTriggerTaskMode.class)
public class TwinTriggerTaskCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinTriggerTaskEntity, TwinTriggerTaskGroupField>, TwinTriggerTaskCountDTOv1> {

    @MapperModePointerBinding(modes = TwinMode.TwinTriggerTask2TwinMode.class)
    private final TwinRestDTOMapperV2 twinDTOMapper;

    @MapperModePointerBinding(modes = TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.TwinTriggerTask2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinTriggerTask2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final TwinTriggerTaskService twinTriggerTaskService;

    @Override
    public void map(CountResult<TwinTriggerTaskEntity, TwinTriggerTaskGroupField> src, TwinTriggerTaskCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setTwinId(entity.getTwinId())
                .setTwinTriggerId(entity.getTwinTriggerId())
                .setPreviousTwinStatusId(entity.getPreviousTwinStatusId())
                .setCreatedByUserId(entity.getCreatedByUserId())
                .setBusinessAccountId(entity.getBusinessAccountId())
                .setStatusId(entity.getStatusId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, TwinMode.TwinTriggerTask2TwinMode.HIDE, src, TwinTriggerTaskGroupField.twinId)) {
            twinTriggerTaskService.loadTwin(entity);
            twinDTOMapper.convertOrPostpone(entity.getTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinTriggerTask2TwinMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.HIDE, src, TwinTriggerTaskGroupField.twinTriggerId)) {
            twinTriggerTaskService.loadTwinTrigger(entity);
            twinTriggerDTOMapper.convertOrPostpone(entity.getTwinTrigger(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.SHORT)));
        }
        if (needLoad(mapperContext, StatusMode.TwinTriggerTask2StatusMode.HIDE, src, TwinTriggerTaskGroupField.previousTwinStatusId)) {
            twinTriggerTaskService.loadPreviousTwinStatus(entity);
            twinStatusDTOMapper.convertOrPostpone(entity.getPreviousTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinTriggerTask2StatusMode.SHORT)));
        }
        if (needLoad(mapperContext, UserMode.TwinTriggerTask2UserMode.HIDE, src, TwinTriggerTaskGroupField.createdByUserId)) {
            twinTriggerTaskService.loadCreatedByUser(entity);
            userDTOMapper.convertOrPostpone(entity.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinTriggerTask2UserMode.SHORT)));
        }
        if (needLoad(mapperContext, BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.HIDE, src, TwinTriggerTaskGroupField.businessAccountId)) {
            twinTriggerTaskService.loadBusinessAccount(entity);
            businessAccountDTOMapper.convertOrPostpone(entity.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinTriggerTaskEntity, TwinTriggerTaskGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var sample = srcCollection.iterator().next();
        if (needLoad(mapperContext, TwinMode.TwinTriggerTask2TwinMode.HIDE, sample, TwinTriggerTaskGroupField.twinId)) {
            twinTriggerTaskService.loadTwins(entityCollection);
        }
        if (needLoad(mapperContext, TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.HIDE, sample, TwinTriggerTaskGroupField.twinTriggerId)) {
            twinTriggerTaskService.loadTwinTriggers(entityCollection);
        }
        if (needLoad(mapperContext, StatusMode.TwinTriggerTask2StatusMode.HIDE, sample, TwinTriggerTaskGroupField.previousTwinStatusId)) {
            twinTriggerTaskService.loadPreviousTwinStatuses(entityCollection);
        }
        if (needLoad(mapperContext, UserMode.TwinTriggerTask2UserMode.HIDE, sample, TwinTriggerTaskGroupField.createdByUserId)) {
            twinTriggerTaskService.loadCreatedByUser(entityCollection);
        }
        if (needLoad(mapperContext, BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.HIDE, sample, TwinTriggerTaskGroupField.businessAccountId)) {
            twinTriggerTaskService.loadBusinessAccounts(entityCollection);
        }
    }
}
