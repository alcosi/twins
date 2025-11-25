package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantTwinRoleMode.class)
public class PermissionGrantTwinRoleRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantTwinRoleEntity, PermissionGrantTwinRoleDTOv1> {
    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantTwinRole2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantTwinRole2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.PermissionGrantTwinRole2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantTwinRole2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;
    @Override
    public void map(PermissionGrantTwinRoleEntity src, PermissionGrantTwinRoleDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionGrantTwinRoleMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinRole(src.getTwinRole())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(src.getGrantedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setTwinClassId(src.getTwinClassId())
                        .setTwinRole(src.getTwinRole());
                break;
        }

        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantTwinRole2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantTwinRole2PermissionSchemaMode.SHORT));
        }

        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantTwinRole2PermissionMode.HIDE)) {
            dst.setPermissionId(src.getPermissionId());
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantTwinRole2PermissionMode.SHORT));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.PermissionGrantTwinRole2TwinClassMode.HIDE)) {
            dst.setTwinClassId(src.getTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.PermissionGrantTwinRole2TwinClassMode.SHORT));
        }

        if (mapperContext.hasModeButNot(UserMode.PermissionGrantTwinRole2UserMode.HIDE)) {
            dst.setGrantedByUserId(src.getGrantedByUserId());
            userRestDTOMapper.postpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantTwinRole2UserMode.SHORT));
        }
    }

    @Override
    public String getObjectCacheId(PermissionGrantTwinRoleEntity src) {
        return src.getId().toString();
    }
}
