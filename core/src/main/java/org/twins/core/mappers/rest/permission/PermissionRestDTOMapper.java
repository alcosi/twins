package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;

@Component
@MapperModeBinding(modes = PermissionMode.class)
public class PermissionRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionDTOv1> {
    @Override
    public void map(PermissionEntity src, PermissionDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setGroupId(src.getPermissionGroupId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionEntity src) {
        return src.getId().toString();
    }
}
