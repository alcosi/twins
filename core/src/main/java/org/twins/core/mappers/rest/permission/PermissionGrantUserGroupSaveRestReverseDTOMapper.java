package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantUserGroupSaveRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupSaveDTOv1, PermissionGrantUserGroupEntity> {

    @Override
    public void map(PermissionGrantUserGroupSaveDTOv1 src, PermissionGrantUserGroupEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setUserGroupId(src.getUserGroupId());
    }
}
