package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleUserGroupMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SpaceRoleUserGroupMode.class)
public class SpaceRoleUserGroupDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserGroupEntity, SpaceRoleUserGroupDTOv1> {

    @MapperModePointerBinding(modes = SpaceRoleMode.SpaceRoleUserGroup2SpaceRoleMode.class)
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @Override
    public void map(SpaceRoleUserGroupEntity src, SpaceRoleUserGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SpaceRoleUserGroupMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserGroupId(src.getUserGroupId())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserGroupId(src.getUserGroupId());
                break;
        }
        if (mapperContext.hasModeButNot(SpaceRoleMode.SpaceRoleUserGroup2SpaceRoleMode.HIDE)) {
            dst.setSpaceRoleId(src.getSpaceRoleId());
            spaceRoleDTOMapper.postpone(src.getSpaceRole(), mapperContext.forkOnPoint(SpaceRoleMode.SpaceRoleUserGroup2SpaceRoleMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(SpaceRoleUserGroupMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleUserGroupEntity src) {
        return src.getId().toString();
    }
}
