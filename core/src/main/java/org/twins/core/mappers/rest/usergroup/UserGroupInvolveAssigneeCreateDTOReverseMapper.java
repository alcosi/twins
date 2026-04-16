package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeCreate;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeCreateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeCreateDTOv1, UserGroupInvolveAssigneeCreate> {
    private final UserGroupInvolveAssigneeSaveDTOReverseMapper userGroupInvolveAssigneeSaveDTOReverseMapper;

    @Override
    public void map(UserGroupInvolveAssigneeCreateDTOv1 src, UserGroupInvolveAssigneeCreate dst, MapperContext mapperContext) throws Exception {
        userGroupInvolveAssigneeSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
