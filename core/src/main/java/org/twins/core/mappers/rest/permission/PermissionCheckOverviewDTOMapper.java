package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.permission.PermissionCheckForTwinOverviewResult;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleByUserDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserDTOMapper;
import org.twins.core.mappers.rest.space.SpaceRoleUserGroupDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionCheckOverviewDTOMapper extends RestSimpleDTOMapper<PermissionCheckForTwinOverviewResult, PermissionCheckOverviewRsDTOv1> {

    final PermissionRestDTOMapper permissionRestDTOMapper;
    final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;
    final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    final UserGroupRestDTOMapper userGroupRestDTOMapper;
    final SpaceRoleByUserDTOMapper spaceRoleByUserDTOMapper;
    final SpaceRoleUserDTOMapper spaceRoleUserDTOMapper;
    final SpaceRoleUserGroupDTOMapper spaceRoleUserGroupDTOMapper;

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
                .setGrantedByUserGroups(userGroupRestDTOMapper.convertList(src.getGrantedByUserGroups().getCollection(), mapperContext))
                .setGrantedByTwinRoles(src.getGrantedByTwinRoles())
                .setGrantedBySpaceRoleUserIds(src.getGrantedBySpaceRoleUsers().getIdSet())
                .setGrantedBySpaceRoleUsers(spaceRoleUserDTOMapper.convertList(src.getGrantedBySpaceRoleUsers().getCollection(), mapperContext))
                .setGrantedBySpaceRoleUserGroupIds(src.getGrantedBySpaceRoleUserGroups().getIdSet())
                .setGrantedBySpaceRoleUserGroups(spaceRoleUserGroupDTOMapper.convertList(src.getGrantedBySpaceRoleUserGroups().getCollection(), mapperContext));
    }
}
