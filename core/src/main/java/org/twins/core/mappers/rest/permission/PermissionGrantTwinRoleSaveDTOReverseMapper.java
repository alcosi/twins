package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantTwinRoleSaveDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantTwinRoleSaveDTOv1, PermissionGrantTwinRoleEntity> {
    @Override
    public void map(PermissionGrantTwinRoleSaveDTOv1 src, PermissionGrantTwinRoleEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setTwinClassId(src.getTwinClassId())
                .setTwinRole(src.getTwinRole());
    }
}
