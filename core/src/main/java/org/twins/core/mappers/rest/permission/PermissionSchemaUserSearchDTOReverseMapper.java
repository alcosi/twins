package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.PermissionSchemaUserSearch;
import org.twins.core.dto.rest.permission.PermissionSchemaUserSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionSchemaUserSearchDTOReverseMapper extends RestSimpleDTOMapper<PermissionSchemaUserSearchRqDTOv1, PermissionSchemaUserSearch> {
    @Override
    public void map(PermissionSchemaUserSearchRqDTOv1 src, PermissionSchemaUserSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setPermissionIdList(src.getPermissionIdList())
                .setPermissionIdExcludeList(src.getPermissionIdExcludeList())
                .setUserIdList(src.getUserIdList())
                .setUserIdExcludeList(src.getUserIdExcludeList())
                .setGrantedByUserIdList(src.getGrantedByUserIdList())
                .setGrantedByUserIdExcludeList(src.getGrantedByUserIdExcludeList());
    }
}
