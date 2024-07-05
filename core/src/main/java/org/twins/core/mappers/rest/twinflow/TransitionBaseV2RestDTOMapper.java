package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.TransitionMode.class)
public class TransitionBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv2> {


    @MapperModePointerBinding(modes = {MapperMode.TransitionOnStatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {MapperMode.TransitionOnPermissionMode.class})
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    private final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {MapperMode.TransitionOnUserMode.class})
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        transitionBaseV1RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(MapperMode.TransitionMode.SHORT)) {
            case DETAILED:
                dst
                        .setSrcTwinStatusId(src.getDstTwinStatusId())
                        .setPermissionId(src.getPermissionId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
        }


        if (mapperContext.hasModeButNot(MapperMode.TransitionOnStatusMode.HIDE))
            dst
                    .setSrcTwinStatusId(src.getSrcTwinStatusId())
                    .setSrcTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getSrcTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TransitionOnStatusMode.SHORT))));
        if (mapperContext.hasModeButNot(MapperMode.TransitionOnPermissionMode.HIDE) && src.getPermissionId() != null)
            dst
                    .setPermissionId(src.getPermissionId())
                    .setPermission(permissionRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadPermission(src), mapperContext.forkOnPoint(MapperMode.TransitionOnPermissionMode.SHORT)));
        if (mapperContext.hasModeButNot(MapperMode.TransitionOnUserMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadCreatedBy(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TransitionOnUserMode.SHORT))));
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.TransitionMode.HIDE);
    }

}
