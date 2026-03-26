package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupInvolveActAsUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.user.UserService;
import org.twins.core.service.usergroup.UserGroupService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = UserGroupInvolveActAsUserMode.class)
public class UserGroupInvolveActAsUserRestDTOMapper extends RestSimpleDTOMapper<UserGroupInvolveActAsUserEntity, UserGroupInvolveActAsUserDTOv1> {
    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;
    private final UserService userService;
    @MapperModePointerBinding(modes = UserGroupMode.User2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final UserGroupService userGroupService;

    @Override
    public void map(UserGroupInvolveActAsUserEntity src, UserGroupInvolveActAsUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setMachineUserId(src.getMachineUserId())
                .setUserGroupId(src.getUserGroupId());

        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            UserEntity userEntity = userService.loadUserAndCheck(src.getMachineUserId());
            userDTOMapper.convertOrPostpone(userEntity, mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
            if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
                userGroupService.loadGroups(userEntity);
                userGroupRestDTOMapper.postpone(userEntity.getUserGroups(), mapperContext.forkOnPoint(UserGroupMode.User2UserGroupMode.SHORT));
            }
        }
    }

//    @Override
//    public boolean hideMode(MapperContext mapperContext) {
//        return mapperContext.hasModeOrEmpty(UserMode.HIDE);
//    }

    @Override
    public String getObjectCacheId(UserGroupInvolveActAsUserEntity src) {
        return src.getId().toString();
    }
}