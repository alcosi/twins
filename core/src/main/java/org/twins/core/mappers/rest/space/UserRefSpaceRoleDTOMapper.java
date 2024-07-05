package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.dto.rest.space.UserWithinSpaceRolesRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class UserRefSpaceRoleDTOMapper extends RestSimpleDTOMapper<UserRefSpaceRole, UserWithinSpaceRolesRsDTOv1> {

    @MapperModePointerBinding(modes = UserMode.SpaceOnUserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @Override
    public void map(UserRefSpaceRole src, UserWithinSpaceRolesRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setUserId(src.getUser().getId());

        if (mapperContext.hasModeButNot(UserMode.SpaceOnUserMode.HIDE))
            dst.setUser(userRestDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.SpaceOnUserMode.SHORT))));

        if (mapperContext.hasModeButNot(SpaceRoleMode.HIDE))
            convertOrPostpone(new Kit<>(src.getRoles().stream().map(SpaceRoleUserEntity::getSpaceRole).toList(), SpaceRoleEntity::getId),
                    dst, spaceRoleDTOMapper,
                    mapperContext.cloneWithIsolatedModes(),
                    UserWithinSpaceRolesRsDTOv1::setSpaceRoleList,
                    UserWithinSpaceRolesRsDTOv1::setSpaceRoleIdsList
            );
    }

    @Override
    public String getObjectCacheId(UserRefSpaceRole src) {
        return src.getUser().getId().toString();
    }
}
