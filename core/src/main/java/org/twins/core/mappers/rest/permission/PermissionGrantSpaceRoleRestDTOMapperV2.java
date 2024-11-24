package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionGrantSpaceRoleRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionGrantSpaceRoleEntity, PermissionGrantSpaceRoleDTOv2> {

    private final PermissionGrantSpaceRoleRestDTOMapper permissionGrantSpaceRoleRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantUserGroup2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantUserGroup2PermissionMode.class)
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;

    @MapperModePointerBinding(modes = SpaceRoleMode.PermissionGrantSpaceRole2SpaceRoleMode.class)
    private final SpaceRoleDTOMapperV2 spaceRoleDTOMapperV2;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantUserGroup2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantSpaceRoleEntity src, PermissionGrantSpaceRoleDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionGrantSpaceRoleRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantSpaceRole2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantSpaceRole2PermissionSchemaMode.SHORT)))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantSpaceRole2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapperV2.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantSpaceRole2PermissionMode.SHORT)))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(SpaceRoleMode.PermissionGrantSpaceRole2SpaceRoleMode.HIDE))
            dst
                    .setSpaceRole(spaceRoleDTOMapperV2.convertOrPostpone(src.getSpaceRole(), mapperContext.forkOnPoint(SpaceRoleMode.PermissionGrantSpaceRole2SpaceRoleMode.SHORT)))
                    .setSpaceRoleId(src.getSpaceRoleId());
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUserGroup2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantUserGroup2UserMode.SHORT)))
                    .setGrantedByUserId(src.getGrantedByUserId());
    }
}
