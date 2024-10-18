package org.twins.core.mappers.rest.permission;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.search.PermissionSearch;
import org.twins.core.dto.rest.permission.PermissionSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;

@Component
public class PermissionSearchDTOReverseMapper extends RestSimpleDTOMapper<PermissionSearchRqDTOv1, PermissionSearch> {
    @Override
    public void map(PermissionSearchRqDTOv1 src, PermissionSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setGroupIdList(src.getGroupIdList())
                .setGroupIdExcludeList(src.getGroupIdExcludeList());
    }
}
