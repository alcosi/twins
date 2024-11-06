package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionSchemaUserGroupEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaUserGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaUserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

@Component
@MapperModeBinding(modes = PermissionSchemaUserGroupMode.class)
@RequiredArgsConstructor
public class PermissionSchemaUserGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionSchemaUserGroupEntity, PermissionSchemaUserGroupDTOv1> {

    @MapperModePointerBinding(modes = {UserMode.PermissionSchemaUserGroup2UserMode.class})
    private final UserRestDTOMapper userRestDTOMapper;
    @MapperModePointerBinding(modes = {UserGroupMode.PermissionSchemaUserGroup2UserGroupMode.class})
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    @MapperModePointerBinding(modes = {PermissionMode.PermissionSchemaUserGroup2PermissionMode.class})
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;


    @Override
    public void map(PermissionSchemaUserGroupEntity src, PermissionSchemaUserGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionSchemaUserGroupMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
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
        if (mapperContext.hasModeButNot(UserMode.PermissionSchemaUserGroup2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionSchemaUserGroup2UserMode.SHORT)))
                    .setGrantedByUserId(src.getGrantedByUserId());
        if (mapperContext.hasModeButNot(UserGroupMode.PermissionSchemaUserGroup2UserGroupMode.HIDE))
            dst
                    .setUserGroup(userGroupRestDTOMapper.convertOrPostpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.PermissionSchemaUserGroup2UserGroupMode.SHORT)))
                    .setUserGroupId(src.getUserGroupId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionSchemaUserGroup2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapperV2.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionSchemaUserGroup2PermissionMode.SHORT)))
                    .setPermissionId(src.getPermissionId());
    }
}
