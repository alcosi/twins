package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.PermissionGrantUserGroupSearch;
import org.twins.core.dto.rest.permission.PermissionGrantUserGroupSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class PermissiongGrantUserGroupSearchRqDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantUserGroupSearchRqDTOv1, PermissionGrantUserGroupSearch> {

    @Override
    public void map(PermissionGrantUserGroupSearchRqDTOv1 src, PermissionGrantUserGroupSearch dst, MapperContext mapperContext) {
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
