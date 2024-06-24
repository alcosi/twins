package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupWithPermissionsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionGroupWithGroupRestDTOMapper extends RestSimpleDTOMapper<ImmutablePair<PermissionGroupEntity, List<PermissionEntity>>, PermissionGroupWithPermissionsDTOv1> {
    final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    final PermissionRestDTOMapper permissionRestDTOMapper;

    @Override
    public void map(ImmutablePair<PermissionGroupEntity, List<PermissionEntity>> src, PermissionGroupWithPermissionsDTOv1 dst, MapperContext mapperContext) throws Exception {
        permissionGroupRestDTOMapper.map(src.getLeft(), dst, mapperContext);
        dst.permissions(permissionRestDTOMapper.convertCollection(src.getRight(), mapperContext));
    }
}
