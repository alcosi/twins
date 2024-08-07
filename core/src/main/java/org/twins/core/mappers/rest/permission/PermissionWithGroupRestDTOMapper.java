package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionWithGroupDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;

@Component
@RequiredArgsConstructor
public class PermissionWithGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionWithGroupDTOv1> {

    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;

    @Override
    public void map(PermissionEntity src, PermissionWithGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        permissionRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.getModeOrUse(PermissionMode.SHORT) == PermissionMode.DETAILED )
            dst.group(permissionGroupRestDTOMapper.convert(src.getPermissionGroup(), mapperContext));
    }
}
