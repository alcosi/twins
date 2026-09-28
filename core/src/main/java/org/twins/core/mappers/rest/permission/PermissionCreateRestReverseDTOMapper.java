package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionCreateRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionCreateRqDTOv1, PermissionEntity> {

    @Override
    public void map(PermissionCreateRqDTOv1 src, PermissionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setPermissionGroupId(src.getGroupId());
    }
}
