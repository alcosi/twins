package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = UserGroupByAssigneePropagationMode.class)
public class UserGroupInvolveAssigneeRestDTOMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeEntity, UserGroupByAssigneePropagationDTOv1> {

    @MapperModePointerBinding(modes = PermissionSchemaMode.UserGroupByAssigneePropagation2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = UserGroupMode.UserGroupByAssigneePropagation2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.UserGroupByAssigneePropagation2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.PropagationTwinStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.UserGroupByAssigneePropagation2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(UserGroupInvolveAssigneeEntity src, UserGroupByAssigneePropagationDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(UserGroupByAssigneePropagationMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setUserGroupId(src.getUserGroupId())
                        .setPropagationTwinClassId(src.getPropagationByTwinClassId())
                        .setPropagationTwinStatusId(src.getPropagationByTwinStatusId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setUserGroupId(src.getUserGroupId())
                        .setPropagationTwinClassId(src.getPropagationByTwinClassId());
                break;
        }

        if (mapperContext.hasModeButNot(PermissionSchemaMode.UserGroupByAssigneePropagation2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.UserGroupByAssigneePropagation2PermissionSchemaMode.SHORT));
        }

        if (mapperContext.hasModeButNot(UserGroupMode.UserGroupByAssigneePropagation2UserGroupMode.HIDE)) {
            dst.setPermissionSchemaId(src.getUserGroupId());
            userGroupRestDTOMapper.postpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.UserGroupByAssigneePropagation2UserGroupMode.SHORT));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.UserGroupByAssigneePropagation2TwinClassMode.HIDE)) {
            dst.setPropagationTwinClassId(src.getPropagationByTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.UserGroupByAssigneePropagation2TwinClassMode.SHORT));
        }

        if (mapperContext.hasModeButNot(StatusMode.PropagationTwinStatus2StatusMode.HIDE)) {
            dst.setPropagationTwinStatusId(src.getPropagationByTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getTwinStatus(), mapperContext.forkOnPoint(StatusMode.PropagationTwinStatus2StatusMode.SHORT));
        }

        if (mapperContext.hasModeButNot(UserMode.UserGroupByAssigneePropagation2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.UserGroupByAssigneePropagation2UserMode.SHORT));
        }
    }

    @Override
    public String getObjectCacheId(UserGroupInvolveAssigneeEntity src) {
        return src.getId().toString();
    }
}
