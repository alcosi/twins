package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantUserGroupUpdateRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupUpdateDTOv1, PermissionGrantUserGroupEntity> {
    private final PermissionGrantUserGroupSaveRestReverseDTOMapper permissionGrantUserGroupSaveRestReverseDTOMapper;

    @Override
    public void map(PermissionGrantUserGroupUpdateDTOv1 src, PermissionGrantUserGroupEntity dst, MapperContext mapperContext) throws Exception {
        permissionGrantUserGroupSaveRestReverseDTOMapper.map(src, dst, mapperContext);
    }
}
