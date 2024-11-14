package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.permission.PermissionSchemaSearch;
import org.twins.core.dto.rest.permission.PermissionSchemaSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PermissionSchemaSearchDTOReverseMapper extends RestSimpleDTOMapper<PermissionSchemaSearchRqDTOv1, PermissionSchemaSearch> {
    @Override
    public void map(PermissionSchemaSearchRqDTOv1 src, PermissionSchemaSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setBusinessAccountIdList(src.getBusinessAccountIdList())
                .setCreatedByUserIdExcludeList(src.getBusinessAccountIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList());
    }
}
