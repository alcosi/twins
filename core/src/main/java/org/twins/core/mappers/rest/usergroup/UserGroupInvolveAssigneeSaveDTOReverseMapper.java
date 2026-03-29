package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeSave;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveAssigneeSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupInvolveAssigneeSaveDTOReverseMapper extends RestSimpleDTOMapper<UserGroupInvolveAssigneeSaveDTOv1, UserGroupInvolveAssigneeSave> {
    @Override
    public void map(UserGroupInvolveAssigneeSaveDTOv1 src, UserGroupInvolveAssigneeSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserGroupId(src.getUserGroupId())
                .setPropagationByTwinClassId(src.getPropagationByTwinClassId())
                .setPropagationByTwinStatusId(src.getPropagationByTwinStatusId());
    }
}
