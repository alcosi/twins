package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveActAsUserSaveDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveActAsUserSaveDTOv1, UserGroupInvolveActAsUserEntity> {
    @Override
    public void map(UserGroupInvolveActAsUserSaveDTOv1 src, UserGroupInvolveActAsUserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setMachineUserId(src.getMachineUserId())
                .setUserGroupId(src.getUserGroupId());
    }
}