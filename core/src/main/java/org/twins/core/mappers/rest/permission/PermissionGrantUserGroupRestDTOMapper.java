package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantUserGroupMode.class)
public class PermissionGrantUserGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupEntity, PermissionGrantUserGroupDTOv1> {

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantUserGroup2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = UserGroupMode.PermissionGrantUserGroup2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantUserGroup2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantUserGroupEntity src, PermissionGrantUserGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantUserGroupMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setUserGroupId(src.getUserGroupId())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(convertOrNull(src.getGrantedAt()));
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setUserGroupId(src.getUserGroupId());
        }
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantUserGroup2PermissionMode.HIDE)) {
            dst.setPermissionId(src.getPermissionId());
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantUserGroup2PermissionMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.PermissionGrantUserGroup2UserGroupMode.HIDE)) {
            dst.setUserGroupId(src.getUserGroupId());
            userGroupRestDTOMapper.postpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.PermissionGrantUserGroup2UserGroupMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUserGroup2UserMode.HIDE)) {
            dst.setGrantedByUserId(src.getGrantedByUserId());
            userRestDTOMapper.postpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantUserGroup2UserMode.SHORT));
        }
    }
}
