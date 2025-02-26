package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantUserGroupMode;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantUserGroupMode.class)
public class PermissionGrantUserGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupEntity, PermissionGrantUserGroupDTOv1> {

    @Override
    public void map(PermissionGrantUserGroupEntity src, PermissionGrantUserGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantUserGroupMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setUserGroupId(src.getUserGroupId())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(convertOrNull(src.getGrantedAt()));
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setUserGroupId(src.getUserGroupId());
        }
    }
}
