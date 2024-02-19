package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.space.SpaceRoleUserMap;
import org.twins.core.dto.rest.space.UserWithinSpaceRolesRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class SpaceRoleUserDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserMap, UserWithinSpaceRolesRsDTOv1> {
    final UserRestDTOMapper userRestDTOMapper;
    final SpaceRoleUserBaseDTOMapper spaceRoleUserDTOMapper;

    @Override
    public void map(SpaceRoleUserMap src, UserWithinSpaceRolesRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getUser().getId());
        dst.setUser(userRestDTOMapper.convert(src.getUser(), mapperContext));
        if (!spaceRoleUserDTOMapper.hideMode(mapperContext))
            dst.setSpaceRoleUserList(spaceRoleUserDTOMapper.convertList(src.getRoles(), mapperContext));
    }

    @Override
    public String getObjectCacheId(SpaceRoleUserMap src) {
        return src.getUser().getId().toString();
    }
}
