package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
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
        dst.setPermissionId(src.getPermission().getId())
                .setPermission(permissionRestDTOMapper.convert(src.getPermission(), mapperContext))
                .setPermissionSchemaId(src.getPermissionSchema().getId())
                .setPermissionSchema(permissionSchemaRestDTOMapper.convert(src.getPermissionSchema(), mapperContext))
                .setPermissionGroupId(src.getPermission().getPermissionGroupId())
                .setPermissionGroup(permissionGroupRestDTOMapper.convert(src.getPermission().getPermissionGroup(), mapperContext))
                .setGrantedByUser(src.isGrantedByUser())
                .setGrantedByUserGroupIds(src.getGrantedByUserGroups().getIdSet())
                .setGrantedByUserGroups(userGroupRestDTOMapper.convertCollection(src.getGrantedByUserGroups().getCollection(), mapperContext))
                .setGrantedByTwinRoles(src.getGrantedByTwinRoles())
                .setPropagatedByTwinClasses(twinClassRestDTOMapper.convertCollection(src.getPropagatedByTwinClasses().getCollection()))
                .setPropagatedByTwinStatuses(twinStatusRestDTOMapper.convertCollection(src.getPropagatedByTwinStatuses().getCollection()))
                .setGrantedBySpaceRoleUserIds(src.getGrantedBySpaceRoleUsers().getIdSet())
                .setGrantedBySpaceRoleUsers(spaceRoleUserDTOMapper.convertCollection(src.getGrantedBySpaceRoleUsers().getCollection(), mapperContext))
                .setGrantedBySpaceRoleUserGroupIds(src.getGrantedBySpaceRoleUserGroups().getIdSet())
                .setGrantedBySpaceRoleUserGroups(spaceRoleUserGroupDTOMapper.convertCollection(src.getGrantedBySpaceRoleUserGroups().getCollection(), mapperContext));
    }
}
