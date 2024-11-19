package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionSchemaUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaUserGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaUserGroupMode;

@Component
@MapperModeBinding(modes = PermissionGrantUserGroupMode.class)
@RequiredArgsConstructor
public class PermissionGrantUserGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupEntity, PermissionGrantUserGroupDTOv1> {
@MapperModeBinding(modes = PermissionSchemaUserGroupMode.class)
public class PermissionSchemaUserGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionSchemaUserGroupEntity, PermissionSchemaUserGroupDTOv1> {

    @Override
    public void map(PermissionGrantUserGroupEntity src, PermissionGrantUserGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantUserGroupMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setUserGroupId(src.getUserGroupId())
                        .setGrantedByUserId(src.getGrantedByUserId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId());
                break;
        }
    }
}
