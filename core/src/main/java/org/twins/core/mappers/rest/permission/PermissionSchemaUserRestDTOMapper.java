package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionSchemaUserEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaUserMode;

@Component
@MapperModeBinding(modes = PermissionSchemaUserMode.class)
public class PermissionSchemaUserRestDTOMapper extends RestSimpleDTOMapper<PermissionSchemaUserEntity, PermissionSchemaUserDTOv1> {
    @Override
    public void map(PermissionSchemaUserEntity src, PermissionSchemaUserDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionSchemaUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setUserId(src.getUserId())
                        .setGrantedByUserId(src.getGrantedByUserId());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionSchemaUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionSchemaUserEntity src) {
        return src.getId().toString();
    }
}
