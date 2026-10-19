package org.twins.core.mappers.rest.usergroup;

import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class UserGroupInvolveActAsUserCreateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveActAsUserCreateDTOv1, UserGroupInvolveActAsUserEntity> {
    @Override
    public void map(UserGroupInvolveActAsUserCreateDTOv1 src, UserGroupInvolveActAsUserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setMachineUserId(src.getMachineUserId())
                .setUserGroupId(src.getUserGroupId());
    }
}
