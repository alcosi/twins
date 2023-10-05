package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupWithPermissionsDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionGroupWithGroupRestDTOMapper extends RestSimpleDTOMapper<ImmutablePair<PermissionGroupEntity, List<PermissionEntity>>, PermissionGroupWithPermissionsDTOv1> {
    final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    final PermissionRestDTOMapper permissionRestDTOMapper;

    @Override
    public void map(ImmutablePair<PermissionGroupEntity, List<PermissionEntity>> src, PermissionGroupWithPermissionsDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        permissionGroupRestDTOMapper.map(src.getLeft(), dst, mapperProperties);
        dst.permissions(permissionRestDTOMapper.convertList(src.getRight(), mapperProperties));
    }
}
