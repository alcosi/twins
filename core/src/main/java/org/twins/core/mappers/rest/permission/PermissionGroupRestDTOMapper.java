package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGroupMode;

@Component
@MapperModeBinding(modes = PermissionGroupMode.class)
public class PermissionGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGroupEntity, PermissionGroupDTOv1> {
    @Override
    public void map(PermissionGroupEntity src, PermissionGroupDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionGroupMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .twinClassId(src.getTwinClassId())
                        .name(src.getName())
                        .description(src.getDescription());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionGroupMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionGroupEntity src) {
        return src.getId().toString();
    }
}
