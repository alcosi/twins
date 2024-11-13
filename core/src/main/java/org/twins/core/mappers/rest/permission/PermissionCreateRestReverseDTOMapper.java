package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionCreateRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionCreateRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionCreateRqDTOv1, PermissionEntity> {

    private final PermissionSaveRestReverseDTOMapper permissionSaveRestReverseDTOMapper;

    @Override
    public void map(PermissionCreateRqDTOv1 src, PermissionEntity dst, MapperContext mapperContext) throws Exception {
        permissionSaveRestReverseDTOMapper.map(src, dst, mapperContext);
    }
}
