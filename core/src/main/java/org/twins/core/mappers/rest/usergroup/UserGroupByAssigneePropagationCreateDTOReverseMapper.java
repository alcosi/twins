package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupByAssigneePropagationEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupByAssigneePropagationCreateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupByAssigneePropagationCreateDTOv1, UserGroupByAssigneePropagationEntity> {
    private final UserGroupByAssigneePropagationSaveDTOReverseMapper userGroupByAssigneePropagationSaveDTOReverseMapper;

    @Override
    public void map(UserGroupByAssigneePropagationCreateDTOv1 src, UserGroupByAssigneePropagationEntity dst, MapperContext mapperContext) throws Exception {
        userGroupByAssigneePropagationSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
