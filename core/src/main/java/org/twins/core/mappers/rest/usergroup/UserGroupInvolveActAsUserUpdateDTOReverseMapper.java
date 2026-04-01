package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveActAsUserUpdateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveActAsUserUpdateDTOv1, UserGroupInvolveActAsUserEntity> {
    private final UserGroupInvolveActAsUserSaveDTOReverseMapper userGroupInvolveActAsUserSaveDTOReverseMapper;

    @Override
    public void map(UserGroupInvolveActAsUserUpdateDTOv1 src, UserGroupInvolveActAsUserEntity dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        userGroupInvolveActAsUserSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}