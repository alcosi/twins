package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.permission.PermissionSchemaUserGroupSearch;
import org.twins.core.dto.rest.permission.PermissionSchemaUserGroupSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissionSchemaSearchRqDTOReverseMapper extends RestSimpleDTOMapper<PermissionSchemaUserGroupSearchRqDTOv1, PermissionSchemaUserGroupSearch> {

    @Override
    public void map(PermissionSchemaUserGroupSearchRqDTOv1 src, PermissionSchemaUserGroupSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setPermissionIdList(src.getPermissionIdList())
                .setPermissionIdExcludeList(src.getPermissionIdExcludeList())
                .setUserGroupIdList(src.getUserGroupIdList())
                .setUserGroupIdExcludeList(src.getUserGroupIdExcludeList())
                .setGrantedByUserIdList(src.getGrantedByUserIdList())
                .setGrantedByUserIdExcludeList(src.getGrantedByUserIdExcludeList());
    }
}
