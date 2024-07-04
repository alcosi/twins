package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@MapperModeBinding(modes = MapperMode.PermissionSchemaMode.class)
public class PermissionSchemaRestDTOMapper extends RestSimpleDTOMapper<PermissionSchemaEntity, PermissionSchemaDTOv1> {
    @Override
    public void map(PermissionSchemaEntity src, PermissionSchemaDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(MapperMode.PermissionSchemaMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .domainId(src.getDomainId())
                        .businessAccountId(src.getBusinessAccountId())
                        .description(src.getDescription());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getName());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.PermissionSchemaMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionSchemaEntity src) {
        return src.getId().toString();
    }
}
