package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantUserUpdateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantUserUpdateDTOv1, PermissionGrantUserEntity> {

    @Override
    public void map(PermissionGrantUserUpdateDTOv1 src, PermissionGrantUserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setUserId(src.getUserId());
    }
}
