package org.twins.core.mappers.rest.twinflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinStatusRestDTOMapper;
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
        switch (mapperContext.getModeOrUse(TransitionBaseV1RestDTOMapper.TransitionMode.SHORT)) {
            case DETAILED:
                dst
                        .setSrcTwinStatusId(src.getDstTwinStatusId())
                        .setPermissionId(src.getPermissionId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
        }
        if (mapperContext.hasModeButNot(TransitionBaseV1RestDTOMapper.TransitionStatusMode.HIDE))
            dst
                    .setSrcTwinStatusId(src.getDstTwinStatusId())
                    .setSrcTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getSrcTwinStatus(), mapperContext.forkOnPoint(TransitionBaseV1RestDTOMapper.TransitionStatusMode.SHORT)));
        if (mapperContext.hasModeButNot(TransitionPermissionMode.HIDE) && src.getPermissionId() != null)
            dst
                    .setPermissionId(src.getDstTwinStatusId())
                    .setPermission(permissionRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadPermission(src), mapperContext.forkOnPoint(TransitionPermissionMode.SHORT)));
        if (mapperContext.hasModeButNot(TransitionAuthorMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUserId(src.getCreatedByUserId())
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(twinflowTransitionService.loadCreatedBy(src), mapperContext.forkOnPoint(TransitionAuthorMode.SHORT)));
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionBaseV1RestDTOMapper.TransitionMode.HIDE);
    }

    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TransitionPermissionMode implements MapperModePointer<PermissionRestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        @Getter
        final int priority;

        @Override
        public PermissionRestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> PermissionRestDTOMapper.Mode.HIDE;
                case SHORT -> PermissionRestDTOMapper.Mode.SHORT;
                case DETAILED -> PermissionRestDTOMapper.Mode.DETAILED;
            };
        }
    }

    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TransitionAuthorMode implements MapperModePointer<UserRestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        @Getter
        final int priority;

        @Override
        public UserRestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> UserRestDTOMapper.Mode.HIDE;
                case SHORT -> UserRestDTOMapper.Mode.SHORT;
                case DETAILED -> UserRestDTOMapper.Mode.DETAILED;
            };
        }
    }
}
