package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantSpaceRoleMode;

@Component
@MapperModeBinding(modes = PermissionGrantSpaceRoleMode.class)
public class PermissionGrantSpaceRoleRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantSpaceRoleEntity, PermissionGrantSpaceRoleDTOv1> {

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
    }
}
