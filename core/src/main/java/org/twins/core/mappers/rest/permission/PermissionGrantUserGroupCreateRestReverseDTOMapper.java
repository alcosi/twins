package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantUserGroupCreateRestReverseDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupCreateRqDTOv1, PermissionGrantUserGroupEntity> {

    @Override
    public void map(PermissionGrantUserGroupCreateRqDTOv1 src, PermissionGrantUserGroupEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionGrantUserGroup().getPermissionSchemaId())
                .setPermissionId(src.getPermissionGrantUserGroup().getPermissionId())
                .setUserGroupId(src.getPermissionGrantUserGroup().getUserGroupId());
    }
}
