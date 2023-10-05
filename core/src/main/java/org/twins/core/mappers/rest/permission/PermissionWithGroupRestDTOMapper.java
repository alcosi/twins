package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionWithGroupDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionWithGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionWithGroupDTOv1> {
    final PermissionRestDTOMapper permissionRestDTOMapper;
    final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;

    @Override
    public void map(PermissionEntity src, PermissionWithGroupDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        permissionRestDTOMapper.map(src, dst, mapperProperties);
        if (mapperProperties.getModeOrUse(PermissionRestDTOMapper.Mode.ID_KEY_ONLY) == PermissionRestDTOMapper.Mode.DETAILED )
            dst.group(permissionGroupRestDTOMapper.convert(src.getPermissionGroup(), mapperProperties));
    }
}
