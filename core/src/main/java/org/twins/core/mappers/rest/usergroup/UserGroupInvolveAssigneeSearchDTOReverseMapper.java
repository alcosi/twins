package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.UserGroupInvolveAssigneeSearch;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeSearchDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeSearchRqDTOv1, UserGroupInvolveAssigneeSearch> {

    @Override
    public void map(UserGroupInvolveAssigneeSearchRqDTOv1 src, UserGroupInvolveAssigneeSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
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
