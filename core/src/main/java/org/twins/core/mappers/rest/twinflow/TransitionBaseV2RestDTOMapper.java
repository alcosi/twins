package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
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
public class TransitionBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv2> {
    final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    final I18nService i18nService;
    final PermissionRestDTOMapper permissionRestDTOMapper;
    final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;
    final TwinflowTransitionService twinflowTransitionService;
    final UserRestDTOMapper userRestDTOMapper;

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
        if (mapperContext.hasModeButNot(MapperMode.TransitionStatusMode.HIDE))
            dst
                    .setSrcTwinStatusId(src.getDstTwinStatusId())
                    .setSrcTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getSrcTwinStatus(), mapperContext.forkOnPoint(MapperMode.TransitionStatusMode.SHORT)));
        if (mapperContext.hasModeButNot(MapperMode.TransitionPermissionMode.HIDE) && src.getPermissionId() != null)
            dst
                    .setPermissionId(src.getDstTwinStatusId())
                    .setPermission(permissionRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadPermission(src), mapperContext.forkOnPoint(MapperMode.TransitionPermissionMode.SHORT)));
        if (mapperContext.hasModeButNot(MapperMode.CreatorMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadCreatedBy(src), mapperContext.forkOnPoint(MapperMode.CreatorMode.SHORT)));
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
