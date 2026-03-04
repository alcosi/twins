package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeUpdateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupByAssigneePropagationUpdateDTOv1, UserGroupInvolveAssigneeEntity> {
    private final UserGroupInvolveAssigneeSaveDTOReverseMapper userGroupInvolveAssigneeSaveDTOReverseMapper;

    @Override
    public void map(UserGroupByAssigneePropagationUpdateDTOv1 src, UserGroupInvolveAssigneeEntity dst, MapperContext mapperContext) throws Exception {
        userGroupInvolveAssigneeSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
