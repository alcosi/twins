package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionUpdateRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionUpdateDTOv1, PermissionEntity> {

    @Override
    public void map(PermissionUpdateDTOv1 src, PermissionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setPermissionGroupId(src.getGroupId())
                .setId(src.getId());
    }
}
