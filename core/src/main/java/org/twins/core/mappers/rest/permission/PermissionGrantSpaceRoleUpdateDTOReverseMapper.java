package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantSpaceRoleUpdateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantSpaceRoleUpdateDTOv1, PermissionGrantSpaceRoleEntity> {
    private final PermissionGrantSpaceRoleSaveDTOReverseMapper permissionGrantSpaceRoleSaveDTOReverseMapper;

    @Override
    public void map(PermissionGrantSpaceRoleUpdateDTOv1 src, PermissionGrantSpaceRoleEntity dst, MapperContext mapperContext) throws Exception {
        permissionGrantSpaceRoleSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}