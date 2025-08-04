package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv3;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;


@Component
@RequiredArgsConstructor
public class TransitionBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv3> {

    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    private final PermissionService permissionService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionMode.SHORT)) {
            case MANAGED -> {
                if (!permissionService.currentUserHasPermission(Permissions.TRANSITION_MANAGE))
                    throw new ServiceException(ErrorCodeTwins.SHOW_MODE_ACCESS_DENIED, "Show Mode[" + TransitionMode.MANAGED + "] is not allowed for current user");
            }
        }
        transitionBaseV2RestDTOMapper.map(src, dst, mapperContext);
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
