package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.UserGroupByAssigneePropagationSearch;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupByAssigneePropagationSearchDTOReverseMapper extends RestSimpleDTOMapper<UserGroupByAssigneePropagationSearchRqDTOv1, UserGroupByAssigneePropagationSearch> {

    @Override
    public void map(UserGroupByAssigneePropagationSearchRqDTOv1 src, UserGroupByAssigneePropagationSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setPermissionSchemaIdList(src.getPermissionSchemaIdList())
                .setPermissionSchemaIdExcludeList(src.getPermissionSchemaIdExcludeList())
                .setUserGroupIdList(src.getUserGroupIdList())
                .setUserGroupIdExcludeList(src.getUserGroupIdExcludeList())
                .setPropagationTwinClassIdList(src.getPropagationTwinClassIdList())
                .setPropagationTwinClassIdExcludeList(src.getPropagationTwinClassIdExcludeList())
                .setPropagationTwinStatusIdList(src.getPropagationTwinStatusIdList())
                .setPropagationTwinStatusIdExcludeList(src.getPropagationTwinStatusIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList());
    }
}
