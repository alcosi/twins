package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantTwinRoleCreateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantTwinRoleCreateDTOv1, PermissionGrantTwinRoleEntity> {
    private final PermissionGrantTwinRoleSaveDTOReverseMapper permissionGrantTwinRoleSaveDTOReverseMapper;

    @Override
    public void map(PermissionGrantTwinRoleCreateDTOv1 src, PermissionGrantTwinRoleEntity dst, MapperContext mapperContext) throws Exception {
        permissionGrantTwinRoleSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
