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
import org.twins.core.service.trigger.TwinTriggerTaskSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class TwinTriggerTaskRestDTOMapper extends RestSimpleDTOMapper<TwinTriggerTaskEntity, TwinTriggerTaskDTOv1> {
    private final TwinTriggerTaskSearchService twinTriggerTaskSearchService;
    private final TwinService twinService;
    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;
    @MapperModePointerBinding(modes = {BusinessAccountMode.class})
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    @MapperModePointerBinding(modes = {StatusMode.Twin2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusDTOMapper;

    private final TwinRestDTOMapperV2 twinDTOMapper;

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

        if (mapperContext.hasModeButNot(BusinessAccountMode.HIDE)) {
            twinTriggerTaskSearchService.loadBusinessAccount(src);
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            twinTriggerTaskSearchService.loadCreatedByUser(src);
            userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Twin2UserMode.SHORT));
        }
        if (mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE)) {
            twinTriggerTaskSearchService.loadTwinStatus(src);
            twinStatusDTOMapper.postpone(src.getPreviousTwinStatus(), mapperContext.forkOnPoint(StatusMode.Twin2StatusMode.SHORT));
        }
        // showMode??
        if (mapperContext.hasModeButNot(TwinMode.HIDE)) {
            twinTriggerTaskSearchService.loadTwin(src);
            twinDTOMapper.postpone(src.getTwin(), mapperContext.forkOnPoint(UserGroupMode.UserGroupInvolveActAsUser2UserGroupMode.SHORT));
        }
        //showMode
        if (mapperContext.hasModeButNot(TwinTriggerMode.HIDE)) {
            twinTriggerTaskSearchService.loadTwinTrigger(src);
            twinTriggerDTOMapper.postpone(src.getTwinTrigger(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinTrigger2FeaturerMode.SHORT)));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinTriggerTaskEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinTriggerTaskEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);

        if (mapperContext.hasModeButNot(BusinessAccountMode.HIDE)) {
            twinTriggerTaskSearchService.loadBusinessAccounts(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            twinTriggerTaskSearchService.loadCreatedByUser(srcCollection);
        }
        if (mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE)) {
            twinTriggerTaskSearchService.loadTwinStatuses(srcCollection);
        }
        // showMode??
        if (mapperContext.hasModeButNot(TwinMode.HIDE)) {
            twinTriggerTaskSearchService.loadTwins(srcCollection);
        }
        //showMode
        if (mapperContext.hasModeButNot(TwinTriggerMode.HIDE)) {
            twinTriggerTaskSearchService.loadTwinTriggers(srcCollection);
        }
    }
}
