package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionSaveRqDTOv1;
import org.twins.core.dto.rest.permission.PermissionUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionUpdateRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionUpdateRqDTOv1, PermissionEntity> {
    private final PermissionRestReverseDTOMapper permissionRestReverseDTOMapper;

    @Override
    public void map(PermissionUpdateRqDTOv1 src, PermissionEntity dst, MapperContext mapperContext) throws Exception {
        permissionRestReverseDTOMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
