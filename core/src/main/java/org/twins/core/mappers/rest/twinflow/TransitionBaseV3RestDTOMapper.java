package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv3;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.validator.TransitionValidatorRuleBaseV1RestDTOMapper;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class TransitionBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv3> {

    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    private final TransitionValidatorRuleBaseV1RestDTOMapper transitionValidatorRuleBaseV1RestDTOMapper;
    private final TriggerV1RestDTOMapper triggerV1RestDTOMapper;

    private final TwinflowTransitionService twinflowTransitionService;
    private final PermissionService permissionService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionMode.SHORT)) {
            case MANAGED:
                if (!permissionService.currentUserHasPermission(Permissions.TRANSITION_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TransitionMode.MANAGED + "] is not allowed for current user");
                twinflowTransitionService.loadValidators(src);
                twinflowTransitionService.loadTriggers(src);
                dst
                        .setValidatorRules(transitionValidatorRuleBaseV1RestDTOMapper.convertCollection(src.getValidatorRulesKit().getCollection(), mapperContext))
                        .setTriggers(triggerV1RestDTOMapper.convertCollection(src.getTriggersKit().getCollection(), mapperContext));
                break;
        }
        transitionBaseV2RestDTOMapper.map(src, dst, mapperContext);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinflowTransitionEntity> srcCollection, MapperContext mapperContext) throws ServiceException {
        if (mapperContext.hasMode(TransitionMode.MANAGED)) {
            if (!permissionService.currentUserHasPermission(Permissions.TRANSITION_MANAGE))
                throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TransitionMode.MANAGED + "] is not allowed for current user");
            twinflowTransitionService.loadValidators(srcCollection);
            twinflowTransitionService.loadTriggers(srcCollection);
        }
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
