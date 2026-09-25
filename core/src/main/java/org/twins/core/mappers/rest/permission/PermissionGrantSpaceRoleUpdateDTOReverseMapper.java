package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantSpaceRoleUpdateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantSpaceRoleUpdateDTOv1, PermissionGrantSpaceRoleEntity> {
    @Override
    public void map(PermissionGrantSpaceRoleUpdateDTOv1 src, PermissionGrantSpaceRoleEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setSpaceRoleId(src.getSpaceRoleId());
    }
}
