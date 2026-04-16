package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupCreate;
import org.twins.core.dto.rest.usergroup.UserGroupCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupCreateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupCreateDTOv1, UserGroupCreate> {
    private final UserGroupSaveDTOReverseMapper userGroupSaveDTOReverseMapper;

    @Override
    public void map(UserGroupCreateDTOv1 src, UserGroupCreate dst, MapperContext mapperContext) throws Exception {
        userGroupSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setUserGroupTypeId(src.getUserGroupTypeId());
    }
}
