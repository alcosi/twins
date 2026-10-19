package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantTwinRoleCreateDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantTwinRoleCreateDTOv1, PermissionGrantTwinRoleEntity> {
    @Override
    public void map(PermissionGrantTwinRoleCreateDTOv1 src, PermissionGrantTwinRoleEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setPermissionId(src.getPermissionId())
                .setTwinClassId(src.getTwinClassId())
                .setGrantedToAssignee(src.getGrantedToAssignee())
                .setGrantedToCreator(src.getGrantedToCreator())
                .setGrantedToSpaceAssignee(src.getGrantedToSpaceAssignee())
                .setGrantedToSpaceCreator(src.getGrantedToSpaceCreator())
        ;
    }
}
