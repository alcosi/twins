package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.dto.rest.space.UserRefSpaceRoleSearchDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class SpaceRoleUserSearchRqDTOReverseMapper extends RestSimpleDTOMapper<UserRefSpaceRoleSearchDTOv1, SpaceRoleUserSearch> {

    @Override
    public void map(UserRefSpaceRoleSearchDTOv1 src, SpaceRoleUserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserNameLike(src.getUserNameLike())
                .setSpaceRolesIdList(convertToSetSafe(src.getSpaceRolesIdList()))
                .setUserGroupIdList(convertToSetSafe(src.getUserGroupIdList()));

    }
}
