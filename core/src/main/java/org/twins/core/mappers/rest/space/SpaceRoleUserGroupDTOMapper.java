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
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.space.SpaceRoleUserGroupService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SpaceRoleUserGroupMode.class)
public class SpaceRoleUserGroupDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserGroupEntity, SpaceRoleUserGroupDTOv1> {

    @MapperModePointerBinding(modes = SpaceRoleMode.SpaceRoleUserGroup2SpaceRoleMode.class)
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;

    @MapperModePointerBinding(modes = TwinMode.SpaceRoleUserGroup2TwinMode.class)
    private final TwinRestDTOMapperV2 twinRestDTOMapper;

    @MapperModePointerBinding(modes = UserGroupMode.SpaceRoleUserGroup2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    private final SpaceRoleUserGroupService spaceRoleUserGroupService;

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
            spaceRoleUserGroupService.loadSpaceRole(src);
            spaceRoleDTOMapper.postpone(src.getSpaceRole(), mapperContext.forkOnPoint(SpaceRoleMode.SpaceRoleUserGroup2SpaceRoleMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinMode.SpaceRoleUserGroup2TwinMode.HIDE)) {
            dst.setTwinId(src.getTwinId());
            spaceRoleUserGroupService.loadTwin(src);
            twinRestDTOMapper.postpone(src.getTwin(), mapperContext.forkOnPoint(TwinMode.SpaceRoleUserGroup2TwinMode.SHORT));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.SpaceRoleUserGroup2UserGroupMode.HIDE)) {
            dst.setUserGroupId(src.getUserGroupId());
            spaceRoleUserGroupService.loadUserGroup(src);
            userGroupRestDTOMapper.postpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.SpaceRoleUserGroup2UserGroupMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<SpaceRoleUserGroupEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(SpaceRoleMode.SpaceRoleUserGroup2SpaceRoleMode.HIDE)) {
            spaceRoleUserGroupService.loadSpaceRole(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinMode.SpaceRoleUserGroup2TwinMode.HIDE)) {
            spaceRoleUserGroupService.loadTwin(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserGroupMode.SpaceRoleUserGroup2UserGroupMode.HIDE)) {
            spaceRoleUserGroupService.loadUserGroup(srcCollection);
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
