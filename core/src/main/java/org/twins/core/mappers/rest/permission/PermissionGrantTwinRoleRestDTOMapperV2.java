package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionGrantTwinRoleRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionGrantTwinRoleEntity, PermissionGrantTwinRoleDTOv2> {
    private final PermissionGrantTwinRoleRestDTOMapper permissionGrantTwinRoleRestDTOMapper;
    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantTwinRole2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantTwinRole2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.PermissionGrantTwinRole2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantTwinRole2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantTwinRoleEntity src, PermissionGrantTwinRoleDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionGrantTwinRoleRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantTwinRole2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantTwinRole2PermissionSchemaMode.SHORT)))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantTwinRole2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapper.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantTwinRole2PermissionMode.SHORT)))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(TwinClassMode.PermissionGrantTwinRole2TwinClassMode.HIDE))
            dst
                    .setTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.PermissionGrantTwinRole2TwinClassMode.SHORT)))
                    .setTwinClassId(src.getTwinClassId());
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantTwinRole2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantTwinRole2UserMode.SHORT)))
                    .setGrantedByUserId(src.getGrantedByUserId());

    }
}
