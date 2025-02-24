package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantSpaceRoleSaveDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantSpaceRoleSaveDTOv1, PermissionGrantSpaceRoleEntity> {
    @Override
    public void map(PermissionGrantSpaceRoleSaveDTOv1 src, PermissionGrantSpaceRoleEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setSpaceRoleId(src.getSpaceRoleId());
    }
}