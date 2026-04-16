package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeUpdate;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeUpdateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeUpdateDTOv1, UserGroupInvolveAssigneeUpdate> {
    private final UserGroupInvolveAssigneeSaveDTOReverseMapper userGroupInvolveAssigneeSaveDTOReverseMapper;

    @Override
    public void map(UserGroupInvolveAssigneeUpdateDTOv1 src, UserGroupInvolveAssigneeUpdate dst, MapperContext mapperContext) throws Exception {
        userGroupInvolveAssigneeSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
