package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@MapperModeBinding(modes = PermissionSchemaMode.class)
public class PermissionSchemaRestDTOMapper extends RestSimpleDTOMapper<PermissionSchemaEntity, PermissionSchemaDTOv1> {
    @Override
    public void map(PermissionSchemaEntity src, PermissionSchemaDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionSchemaMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDomainId(src.getDomainId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setDescription(src.getDescription())
                        .setCreatedAt(convertOrNull(src.getCreatedAt()));
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getName());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionSchemaMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionSchemaEntity src) {
        return src.getId().toString();
    }
}
