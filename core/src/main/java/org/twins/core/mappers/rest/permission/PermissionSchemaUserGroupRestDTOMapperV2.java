package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionSchemaUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaUserGroupDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionSchemaUserGroupMode.class)
public class PermissionSchemaUserGroupRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionSchemaUserGroupEntity, PermissionSchemaUserGroupDTOv2> {

    private final PermissionSchemaUserGroupRestDTOMapper permissionSchemaUserGroupRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionSchemaUserGroup2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionSchemaUserGroup2PermissionMode.class)
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;

    @MapperModePointerBinding(modes = UserGroupMode.PermissionSchemaUserGroup2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionSchemaUserGroup2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionSchemaUserGroupEntity src, PermissionSchemaUserGroupDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionSchemaUserGroupRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionSchemaUserGroup2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionSchemaUserGroup2PermissionSchemaMode.SHORT)))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionSchemaUserGroup2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapperV2.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionSchemaUserGroup2PermissionMode.SHORT)))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(UserGroupMode.PermissionSchemaUserGroup2UserGroupMode.HIDE))
            dst
                    .setUserGroup(userGroupRestDTOMapper.convertOrPostpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.PermissionSchemaUserGroup2UserGroupMode.SHORT)))
                    .setUserGroupId(src.getUserGroupId());
        if (mapperContext.hasModeButNot(UserMode.PermissionSchemaUserGroup2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionSchemaUserGroup2UserMode.SHORT)))
                    .setGrantedByUserId(src.getGrantedByUserId());
    }
}
