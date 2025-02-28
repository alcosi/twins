package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionGrantUserSaveDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantUserSaveDTOv1, PermissionGrantUserEntity> {

    @Override
    public void map(PermissionGrantUserSaveDTOv1 src, PermissionGrantUserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setUserId(src.getUserId());
    }
}
