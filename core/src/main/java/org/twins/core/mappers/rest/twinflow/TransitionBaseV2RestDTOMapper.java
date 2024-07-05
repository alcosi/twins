package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv2;
import org.twins.core.mappers.rest.mappercontext.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionMode.class)
public class TransitionBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv2> {


    @MapperModePointerBinding(modes = {StatusMode.TransitionOnStatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {PermissionMode.TransitionOnPermissionMode.class})
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    private final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {UserMode.TransitionOnUserMode.class})
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        transitionBaseV1RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TransitionMode.SHORT)) {
            case DETAILED:
                dst
                        .setSrcTwinStatusId(src.getDstTwinStatusId())
                        .setPermissionId(src.getPermissionId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
        }


        if (mapperContext.hasModeButNot(StatusMode.TransitionOnStatusMode.HIDE))
            dst
                    .setSrcTwinStatusId(src.getSrcTwinStatusId())
                    .setSrcTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getSrcTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TransitionOnStatusMode.SHORT))));
        if (mapperContext.hasModeButNot(PermissionMode.TransitionOnPermissionMode.HIDE) && src.getPermissionId() != null)
            dst
                    .setPermissionId(src.getPermissionId())
                    .setPermission(permissionRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadPermission(src), mapperContext.forkOnPoint(PermissionMode.TransitionOnPermissionMode.SHORT)));
        if (mapperContext.hasModeButNot(UserMode.TransitionOnUserMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadCreatedBy(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TransitionOnUserMode.SHORT))));
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
