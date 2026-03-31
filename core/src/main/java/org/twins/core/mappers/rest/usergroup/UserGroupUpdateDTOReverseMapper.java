package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupUpdate;
import org.twins.core.dto.rest.usergroup.UserGroupUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupUpdateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupUpdateDTOv1, UserGroupUpdate> {
    private final UserGroupSaveDTOReverseMapper userGroupSaveDTOReverseMapper;

    @Override
    public void map(UserGroupUpdateDTOv1 src, UserGroupUpdate dst, MapperContext mapperContext) throws Exception {
        userGroupSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
