package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserGroupDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.SpaceRoleUserGroupMode.class)
public class SpaceRoleUserGroupDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserGroupEntity, SpaceRoleUserGroupDTOv1> {
    @MapperModePointerBinding(modes = MapperMode.SpaceRoleUserGroup2SpaceRoleMode.class)
    final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @Override
    public void map(SpaceRoleUserGroupEntity src, SpaceRoleUserGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.SpaceRoleUserGroupMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserGroupId(src.getUserGroupId())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setSpaceRole(spaceRoleDTOMapper.convert(src.getSpaceRole(), mapperContext));
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setTwinId(src.getTwinId())
                        .setSpaceRoleId(src.getSpaceRoleId())
                        .setUserGroupId(src.getUserGroupId());
                break;
        }
        if (mapperContext.hasModeButNot(MapperMode.SpaceRoleUserGroup2SpaceRoleMode.HIDE))
            dst
                    .setSpaceRole(spaceRoleDTOMapper.convert(src.getSpaceRole(), mapperContext
                            .forkOnPoint(MapperMode.SpaceRoleUserGroup2SpaceRoleMode.SHORT)));
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.SpaceRoleUserGroupMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleUserGroupEntity src) {
        return src.getId().toString();
    }
}
