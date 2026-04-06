package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.trigger.TwinTriggerTaskService;

import java.util.Collection;
import java.util.Set;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainMode.class)
public class TwinTriggerTaskRestDTOMapper extends RestSimpleDTOMapper<TwinTriggerTaskEntity, TwinTriggerTaskDTOv1> {
    @MapperModePointerBinding(modes = {BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.class})
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final TwinTriggerTaskService twinTriggerTaskService;

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

        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.HIDE)) {

            businessAccountDTOMapper.postpone((src.getBusinessAccountId()), mapperContext.forkOnPoint(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.SHORT));
        }

        if (mapperContext.hasModeButNot(UserMode.UserGroupInvolveActAsUser2UserMode.HIDE)) {
            userGroupInvolveActAsUserService.loadAddedByUser(src);
            userGroupInvolveActAsUserService.loadMachineUser(src);
            userDTOMapper.convertOrPostpone(src.getAddedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.UserGroupInvolveActAsUser2UserMode.SHORT)));
            userDTOMapper.convertOrPostpone(src.getMachineUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.UserGroupInvolveActAsUser2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            userGroupInvolveActAsUserService.loadUserGroup(src);
            userGroupRestDTOMapper.postpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.UserGroupInvolveActAsUser2UserGroupMode.SHORT));
        }


        if (mapperContext.hasModeButNot(FeaturerMode.TwinTrigger2FeaturerMode.HIDE)) {
            twinTriggerService.loadTwinTriggerFeaturer(src);
            dst.setTriggerFeaturerId(src.getTwinTriggerFeaturerId());
            featurerRestDTOMapper.postpone(src.getTwinTriggerFeaturer(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TwinTrigger2FeaturerMode.SHORT)));
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
        if (mapperContext.hasModeButNot(FeaturerMode.TwinTrigger2FeaturerMode.HIDE)) {
            twinTriggerService.loadTwinTriggerFeaturer(srcCollection);
        }
    }
}
