package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.dto.rest.space.UserRefSpaceRoleSearchDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class SpaceRoleUserSearchRqDTOReverseMapper extends RestSimpleDTOMapper<UserRefSpaceRoleSearchDTOv1, SpaceRoleUserSearch> {

    @Override
    public void map(UserRefSpaceRoleSearchDTOv1 src, SpaceRoleUserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserNameLike(src.getUserNameLike())
                .setSpaceRolesIdList(convertSafe(src.getSpaceRolesIdList()))
                .setUserGroupIdList(convertSafe(src.getUserGroupIdList()));

    }

    private <T> Set<T> convertSafe(List<T> list) {
        if (list == null)
            return null;
        return Set.copyOf(list);
    }

}
