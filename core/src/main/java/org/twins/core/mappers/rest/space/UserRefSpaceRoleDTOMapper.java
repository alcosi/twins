package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.dto.rest.space.UserWithinSpaceRolesRsDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRefSpaceRoleDTOMapper extends RestSimpleDTOMapper<UserRefSpaceRole, UserWithinSpaceRolesRsDTOv1> {

    @MapperModePointerBinding(modes = UserMode.Space2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @Override
    public void map(UserRefSpaceRole src, UserWithinSpaceRolesRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setUserId(src.getUser().getId());
        if (mapperContext.hasModeButNot(UserMode.Space2UserMode.HIDE)) {
            userRestDTOMapper.postpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Space2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(SpaceRoleMode.HIDE)) {
            Kit<SpaceRoleEntity, UUID> spaceRoles = new Kit<>(src.getRoles().stream().map(SpaceRoleUserEntity::getSpaceRole).toList(), SpaceRoleEntity::getId);
            dst.setSpaceRoleIdsList(spaceRoles.getIdSet());
            spaceRoleDTOMapper.postpone(spaceRoles, mapperContext);
        }
    }

    @Override
    public String getObjectCacheId(UserRefSpaceRole src) {
        return src.getUser().getId().toString();
    }
}
