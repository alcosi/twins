package org.twins.core.mappers.rest.usergroup;

import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeCreate;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class UserGroupInvolveAssigneeCreateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeCreateDTOv1, UserGroupInvolveAssigneeCreate> {
    @Override
    public void map(UserGroupInvolveAssigneeCreateDTOv1 src, UserGroupInvolveAssigneeCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserGroupId(src.getUserGroupId())
                .setPropagationByTwinClassId(src.getPropagationByTwinClassId())
                .setPropagationByTwinStatusId(src.getPropagationByTwinStatusId());
    }
}
