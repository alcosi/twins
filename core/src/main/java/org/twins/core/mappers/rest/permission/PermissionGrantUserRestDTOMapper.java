package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantUserMode;

@Component
@MapperModeBinding(modes = PermissionGrantUserMode.class)
public class PermissionGrantUserRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserEntity, PermissionGrantUserDTOv1> {
    @Override
    public void map(PermissionGrantUserEntity src, PermissionGrantUserDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionGrantUserMode.DETAILED)) {
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
        return mapperContext.hasModeOrEmpty(PermissionGrantUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionGrantUserEntity src) {
        return src.getId().toString();
    }
}
