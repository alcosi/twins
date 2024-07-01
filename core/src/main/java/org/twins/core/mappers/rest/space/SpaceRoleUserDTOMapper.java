package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.SpaceRoleUserMode.class)
public class SpaceRoleUserDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserEntity, SpaceRoleUserDTOv1> {
    @MapperModePointerBinding(modes = MapperMode.SpaceRoleUser2SpaceRoleMode.class)
    final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @Override
    public void map(SpaceRoleUserEntity src, SpaceRoleUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.SpaceRoleUserMode.DETAILED)) {
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
        if (mapperContext.hasModeButNot(MapperMode.SpaceRoleUser2SpaceRoleMode.HIDE))
            dst
                    .setSpaceRole(spaceRoleDTOMapper.convert(src.getSpaceRole(), mapperContext
                            .forkOnPoint(MapperMode.SpaceRoleUser2SpaceRoleMode.SHORT)));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.SpaceRoleUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleUserEntity src) {
        return src.getId().toString();
    }
}
