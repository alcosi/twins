package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionGrantUserGroupRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionGrantUserGroupEntity, PermissionGrantUserGroupDTOv2> {

    private final PermissionGrantUserGroupRestDTOMapper permissionGrantUserGroupRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantUserGroup2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = UserGroupMode.PermissionGrantUserGroup2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantUserGroup2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantUserGroupEntity src, PermissionGrantUserGroupDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionGrantUserGroupRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.SHORT)))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantUserGroup2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapper.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantUserGroup2PermissionMode.SHORT)))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(UserGroupMode.PermissionGrantUserGroup2UserGroupMode.HIDE))
            dst
                    .setUserGroup(userGroupRestDTOMapper.convertOrPostpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.PermissionGrantUserGroup2UserGroupMode.SHORT)))
                    .setUserGroupId(src.getUserGroupId());
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUserGroup2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantUserGroup2UserMode.SHORT)))
                    .setGrantedByUserId(src.getGrantedByUserId());
    }
}
