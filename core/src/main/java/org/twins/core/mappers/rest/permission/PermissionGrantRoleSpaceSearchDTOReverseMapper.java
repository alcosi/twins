package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.PermissionGrantSpaceRoleSearch;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantRoleSpaceSearchDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantSpaceRoleSearchRqDTOv1, PermissionGrantSpaceRoleSearch> {
    @Override
    public void map(PermissionGrantSpaceRoleSearchRqDTOv1 src, PermissionGrantSpaceRoleSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setPermissionIdList(src.getPermissionIdList())
                .setPermissionIdExcludeList(src.getPermissionIdExcludeList())
                .setSpaceRoleIdList(src.getSpaceRoleIdList())
                .setSpaceRoleIdExcludeList(src.getSpaceRoleIdExcludeList())
                .setGrantedByUserIdList(src.getGrantedByUserIdList())
                .setGrantedByUserIdExcludeList(src.getGrantedByUserIdExcludeList());
    }
}
