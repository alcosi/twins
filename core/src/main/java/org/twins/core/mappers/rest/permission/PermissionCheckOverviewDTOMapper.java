package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.space.SpaceRoleUserDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserGroupDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionCheckOverviewDTOMapper extends RestSimpleDTOMapper<PermissionCheckForTwinOverviewResult, PermissionCheckOverviewRsDTOv1> {

    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final SpaceRoleUserDTOMapper spaceRoleUserDTOMapper;
    private final SpaceRoleUserGroupDTOMapper spaceRoleUserGroupDTOMapper;
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @Override
    public void map(PermissionCheckForTwinOverviewResult src, PermissionCheckOverviewRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionId(src.getPermission().getId())
                .setPermissionSchemaId(src.getPermissionSchema().getId())
                .setPermissionGroupId(src.getPermission().getPermissionGroupId())
                .setGrantedByUser(src.isGrantedByUser())
                .setGrantedByUserGroupIds(src.getGrantedByUserGroups().getIdSet())
                .setGrantedByTwinRoles(src.getGrantedByTwinRoles())
                .setGrantedBySpaceRoleUserIds(src.getGrantedBySpaceRoleUsers().getIdSet())
                .setGrantedBySpaceRoleUserGroupIds(src.getGrantedBySpaceRoleUserGroups().getIdSet());
        permissionRestDTOMapper.postpone(src.getPermission(), mapperContext);
        permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext);
        permissionGroupRestDTOMapper.postpone(src.getPermission().getPermissionGroup(), mapperContext);
        userGroupRestDTOMapper.postpone(src.getGrantedByUserGroups().getCollection(), mapperContext);
        twinClassRestDTOMapper.postpone(src.getPropagatedByTwinClasses().getCollection(), mapperContext);
        twinStatusRestDTOMapper.postpone(src.getPropagatedByTwinStatuses().getCollection(), mapperContext);
        spaceRoleUserDTOMapper.postpone(src.getGrantedBySpaceRoleUsers().getCollection(), mapperContext);
        spaceRoleUserGroupDTOMapper.postpone(src.getGrantedBySpaceRoleUserGroups().getCollection(), mapperContext);
    }
}
