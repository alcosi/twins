package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantUserUpdateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantUserUpdateDTOv1, PermissionGrantUserEntity> {
    private final PermissionGrantUserSaveDTOReverseMapper permissionGrantUserSaveDTOReverseMapper;

    @Override
    public void map(PermissionGrantUserUpdateDTOv1 src, PermissionGrantUserEntity dst, MapperContext mapperContext) throws Exception {
        permissionGrantUserSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
