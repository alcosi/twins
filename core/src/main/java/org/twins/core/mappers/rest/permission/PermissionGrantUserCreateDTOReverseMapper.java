package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantUserCreateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantUserCreateDTOv1, PermissionGrantUserEntity> {

    @Override
    public void map(PermissionGrantUserCreateDTOv1 src, PermissionGrantUserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setUserId(src.getUserId());
    }
}
