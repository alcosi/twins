package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantSpaceRoleMode.class)
public class PermissionGrantSpaceRoleRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantSpaceRoleEntity, PermissionGrantSpaceRoleDTOv1> {

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantSpaceRole2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantSpaceRole2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = SpaceRoleMode.PermissionGrantSpaceRole2SpaceRoleMode.class)
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantSpaceRole2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantSpaceRoleEntity src, PermissionGrantSpaceRoleDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantSpaceRoleMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(src.getGrantedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setSpaceRoleId(src.getSpaceRoleId());
                break;
        }

        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantSpaceRole2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantSpaceRole2PermissionSchemaMode.SHORT));
        }

        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantSpaceRole2PermissionMode.HIDE)) {
            dst.setPermissionId(src.getPermissionId());
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantSpaceRole2PermissionMode.SHORT));
        }

        if (mapperContext.hasModeButNot(SpaceRoleMode.PermissionGrantSpaceRole2SpaceRoleMode.HIDE)) {
            dst.setSpaceRoleId(src.getSpaceRoleId());
            spaceRoleDTOMapper.postpone(src.getSpaceRole(), mapperContext.forkOnPoint(SpaceRoleMode.PermissionGrantSpaceRole2SpaceRoleMode.SHORT));
        }

        if (mapperContext.hasModeButNot(UserMode.PermissionGrantSpaceRole2UserMode.HIDE)) {
            dst.setGrantedByUserId(src.getGrantedByUserId());
            userRestDTOMapper.postpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantSpaceRole2UserMode.SHORT));
        }
    }

    @Override
    public String getObjectCacheId(PermissionGrantSpaceRoleEntity src) {
        return src.getId().toString();
    }
}
