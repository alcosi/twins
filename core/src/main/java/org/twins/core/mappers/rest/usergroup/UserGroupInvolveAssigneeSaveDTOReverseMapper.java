package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dto.rest.usergroup.UserGroupByAssigneePropagationSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeSaveDTOReverseMapper extends RestSimpleDTOMapper<UserGroupByAssigneePropagationSaveDTOv1, UserGroupInvolveAssigneeEntity> {
    @Override
    public void map(UserGroupByAssigneePropagationSaveDTOv1 src, UserGroupInvolveAssigneeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setUserGroupId(src.getUserGroupId())
                .setPropagationByTwinClassId(src.getPropagationByTwinClassId())
                .setPropagationByTwinStatusId(src.getPropagationByTwinStatusId());
    }
}
