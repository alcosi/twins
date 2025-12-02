package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleUserMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SpaceRoleUserMode.class)
public class SpaceRoleUserDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserEntity, SpaceRoleUserDTOv1> {

    @MapperModePointerBinding(modes = SpaceRoleMode.SpaceRoleUser2SpaceRoleMode.class)
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @Override
    public void map(SpaceRoleUserEntity src, SpaceRoleUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SpaceRoleUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserId(src.getUserId())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserId(src.getUserId());
                break;
        }
        if (mapperContext.hasModeButNot(SpaceRoleMode.SpaceRoleUser2SpaceRoleMode.HIDE)) {
            dst.setSpaceRoleId(src.getSpaceRoleId());
            spaceRoleDTOMapper.postpone(src.getSpaceRole(), mapperContext.forkOnPoint(SpaceRoleMode.SpaceRoleUser2SpaceRoleMode.SHORT));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(SpaceRoleUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleUserEntity src) {
        return src.getId().toString();
    }
}
