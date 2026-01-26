package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionGroupWithGroupRestDTOMapper extends RestSimpleDTOMapper<ImmutablePair<PermissionGroupEntity, List<PermissionEntity>>, PermissionGroupDTOv1> {
    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;

    @Override
    public void map(ImmutablePair<PermissionGroupEntity, List<PermissionEntity>> src, PermissionGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        permissionGroupRestDTOMapper.map(src.getLeft(), dst, mapperContext);
        dst.setPermissionIds(src.getRight().stream().map(PermissionEntity::getId).toList());
        permissionRestDTOMapper.postpone(src.getRight(), mapperContext);
    }
}
