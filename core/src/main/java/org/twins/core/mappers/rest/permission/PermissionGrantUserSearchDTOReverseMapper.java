package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.PermissionGrantUserSearch;
import org.twins.core.dto.rest.permission.PermissionGrantUserSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantUserSearchDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantUserSearchRqDTOv1, PermissionGrantUserSearch> {
    @Override
    public void map(PermissionGrantUserSearchRqDTOv1 src, PermissionGrantUserSearch dst, MapperContext mapperContext) {
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
