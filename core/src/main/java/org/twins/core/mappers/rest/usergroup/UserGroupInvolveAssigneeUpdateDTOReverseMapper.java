package org.twins.core.mappers.rest.usergroup;

import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeUpdate;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class UserGroupInvolveAssigneeUpdateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeUpdateDTOv1, UserGroupInvolveAssigneeUpdate> {
    @Override
    public void map(UserGroupInvolveAssigneeUpdateDTOv1 src, UserGroupInvolveAssigneeUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        dst
                .setUserGroupId(src.getUserGroupId())
                .setPropagationByTwinClassId(src.getPropagationByTwinClassId())
                .setPropagationByTwinStatusId(src.getPropagationByTwinStatusId());
    }
}
