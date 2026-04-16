package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.trigger.TwinTriggerTaskService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class TwinTriggerTaskRestDTOMapper extends RestSimpleDTOMapper<TwinTriggerTaskEntity, TwinTriggerTaskDTOv1> {
    private final TwinTriggerTaskService twinTriggerTaskService;
    @MapperModePointerBinding(modes = {UserMode.class})
    private final UserRestDTOMapper userDTOMapper;
    @MapperModePointerBinding(modes = {BusinessAccountMode.class})
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    @MapperModePointerBinding(modes = {StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusDTOMapper;
    @MapperModePointerBinding(modes = {TwinMode.class})
    private final TwinRestDTOMapperV2 twinDTOMapper;
    @MapperModePointerBinding(modes = {TwinTriggerMode.class})
    private final TwinTriggerRestDTOMapper twinTriggerDTOMapper;


    @Override
    public void map(TwinTriggerTaskEntity src, TwinTriggerTaskDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinId(src.getTwinId())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setPreviousTwinStatusId(src.getPreviousTwinStatusId())
                    .setBusinessAccountId(src.getBusinessAccountId())
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setStatusDetails(src.getStatusDetails())
                    .setStatusId(src.getStatusId())
                    .setCreatedAt(src.getCreatedAt())
                    .setDoneAt(src.getDoneAt());
            case SHORT -> dst
                    .setId(src.getId());
        }

        if (mapperContext.hasModeButNot(BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.HIDE)) {
            twinTriggerTaskService.loadBusinessAccount(src);
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.TwinTriggerTask2UserMode.HIDE)) {
            twinTriggerTaskService.loadCreatedByUser(src);
            userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinTriggerTask2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(StatusMode.TwinTriggerTask2StatusMode.HIDE)) {
            twinStatusDTOMapper.postpone(src.getPreviousTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinTriggerTask2StatusMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinMode.TwinTriggerTask2TwinMode.HIDE)) {
            twinDTOMapper.postpone(src.getTwin(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinTriggerTask2TwinMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.HIDE)) {
            twinTriggerTaskService.loadTwinTrigger(src);
            twinTriggerDTOMapper.postpone(src.getTwinTrigger(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.SHORT)));
        }
    }

    @Override
    public String getObjectCacheId(TwinTriggerTaskEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinTriggerTaskEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);

        if (mapperContext.hasModeButNot(BusinessAccountMode.TwinTriggerTask2BusinessAccountMode.HIDE)) {
            twinTriggerTaskService.loadBusinessAccounts(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserMode.TwinTriggerTask2UserMode.HIDE)) {
            twinTriggerTaskService.loadCreatedByUser(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinTriggerMode.TwinTriggerTask2TwinTriggerMode.HIDE)) {
            twinTriggerTaskService.loadTwinTriggers(srcCollection);
        }
    }
}
