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
        dst.setPermissionId(src.getPermissionId())
                .setPermissionSchemaIds(src.getPermissionSchemaIds())
                .setPermissionGroupId(src.getPermissionGroupId())
                .setPermission(permissionRestDTOMapper.convert(src.getPermission(), mapperContext))
                .setPermissionSchemas(permissionSchemaRestDTOMapper.convertList(src.getPermissionSchemas(), mapperContext))
                .setPermissionGroup(permissionGroupRestDTOMapper.convert(src.getPermissionGroup(), mapperContext))
                .setGrantedByUser(src.isGrantedByUser())
                .setGrantedByUserGroupIds(src.getGrantedByUserGroupIds())
                .setGrantedByUserGroups(userGroupRestDTOMapper.convertList(src.getGrantedByUserGroups(), mapperContext))
                .setGrantedByTwinRoles(src.getGrantedByTwinRoles())
                .setSpaceRoles(spaceRoleByUserDTOMapper.convertList(src.getGrantedBySpaceRoleUsers(), mapperContext))
                .setGrantedBySpaceRoleUserIds(src.getGrantedBySpaceRoleUserIds())
                .setGrantedBySpaceRoleUsers(spaceRoleUserDTOMapper.convertList(src.getGrantedBySpaceRoleUsers(), mapperContext))
                .setGrantedBySpaceRoleUserGroupIds(src.getGrantedBySpaceRoleUserGroupIds())
                .setGrantedBySpaceRoleUserGroups(spaceRoleUserGroupDTOMapper.convertList(src.getGrantedBySpaceRoleUserGroups(), mapperContext));
    }
}
