package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.PermissionGrantTwinRoleSearch;
import org.twins.core.dto.rest.permission.PermissionGrantTwinRoleSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionGrantTwinRoleSearchRqDTOReverseMapper extends RestSimpleDTOMapper<PermissionGrantTwinRoleSearchRqDTOv1, PermissionGrantTwinRoleSearch> {

    @Override
    public void map(PermissionGrantTwinRoleSearchRqDTOv1 src, PermissionGrantTwinRoleSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setPermissionIdList(src.getPermissionIdList())
                .setPermissionIdExcludeList(src.getPermissionIdExcludeList())
                .setTwinClassIdList(src.getTwinClassIdList())
                .setTwinClassIdExcludeList(src.getTwinClassIdExcludeList())
                .setIsAssignee(src.getIsAssignee())
                .setIsSpaceAssignee(src.getIsSpaceAssignee())
                .setIsCreator(src.getIsCreator())
                .setIsSpaceCreator(src.getIsSpaceCreator())
                .setGrantedByUserIdList(src.getGrantedByUserIdList())
                .setGrantedByUserIdExcludeList(src.getGrantedByUserIdExcludeList());
    }
}
