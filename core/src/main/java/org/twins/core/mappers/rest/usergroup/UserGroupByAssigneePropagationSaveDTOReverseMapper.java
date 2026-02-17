package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupByAssigneePropagationEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupByAssigneePropagationSaveDTOReverseMapper extends RestSimpleDTOMapper<UserGroupByAssigneePropagationSaveDTOv1, UserGroupByAssigneePropagationEntity> {
    @Override
    public void map(UserGroupByAssigneePropagationSaveDTOv1 src, UserGroupByAssigneePropagationEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setUserGroupId(src.getUserGroupId())
                .setPropagationByTwinClassId(src.getPropagationByTwinClassId())
                .setPropagationByTwinStatusId(src.getPropagationByTwinStatusId());
    }
}
