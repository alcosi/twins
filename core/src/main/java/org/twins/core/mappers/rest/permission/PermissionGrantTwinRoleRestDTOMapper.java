package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantTwinRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantUserMode;

@Component
@MapperModeBinding(modes = PermissionGrantTwinRoleMode.class)
public class PermissionGrantTwinRoleRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantTwinRoleEntity, PermissionGrantTwinRoleDTOv1> {
    @Override
    public void map(PermissionGrantTwinRoleEntity src, PermissionGrantTwinRoleDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionGrantUserMode.DETAILED)) {
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
    }
}
