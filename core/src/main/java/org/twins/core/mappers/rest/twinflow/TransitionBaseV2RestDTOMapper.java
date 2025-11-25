package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.factory.FactoryRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionMode.class)
public class TransitionBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinflowTransitionBaseDTOv2> {

    private final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {StatusMode.Transition2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {PermissionMode.Transition2PermissionMode.class})
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinflowMode.Transition2TwinflowMode.class)
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = {UserMode.Transition2UserMode.class})
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.Transition2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    private final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowTransitionEntity src, TwinflowTransitionBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        transitionBaseV1RestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(TransitionMode.SHORT)) {
            case DETAILED, MANAGED:
                dst
                        .setSrcTwinStatusId(src.getSrcTwinStatusId())
                        .setPermissionId(src.getPermissionId())
                        .setTwinflowId(src.getTwinflowId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
        }
        if (mapperContext.hasModeButNot(StatusMode.Transition2StatusMode.HIDE) && src.getSrcTwinStatusId() != null) {
            dst.setSrcTwinStatusId(src.getSrcTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getSrcTwinStatus(), mapperContext.forkOnPoint(StatusMode.Transition2StatusMode.SHORT));
        }
        if (mapperContext.hasModeButNot(PermissionMode.Transition2PermissionMode.HIDE) && src.getPermissionId() != null) {
            dst.setPermissionId(src.getPermissionId());
            permissionRestDTOMapper.postpone(twinflowTransitionService.loadPermission(src), mapperContext.forkOnPoint(PermissionMode.Transition2PermissionMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinflowMode.Transition2TwinflowMode.HIDE) && src.getTwinflowId() != null) {
            dst.setTwinflowId(src.getTwinflowId());
            twinflowBaseV1RestDTOMapper.postpone(src.getTwinflow(), mapperContext.forkOnPoint(TwinflowMode.Transition2TwinflowMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserMode.Transition2UserMode.HIDE) && src.getCreatedByUserId() != null) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(twinflowTransitionService.loadCreatedBy(src), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Transition2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(FactoryMode.Transition2FactoryMode.HIDE)) {
            dst.setInbuiltTwinFactoryId(src.getInbuiltTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getInbuiltFactory(), mapperContext.forkOnPoint(FactoryMode.Transition2FactoryMode.SHORT));
            dst.setDraftingTwinFactoryId(src.getDraftingTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getDraftingFactory(), mapperContext.forkOnPoint(FactoryMode.Transition2FactoryMode.SHORT));
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
