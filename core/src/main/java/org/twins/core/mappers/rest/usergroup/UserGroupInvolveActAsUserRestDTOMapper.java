package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
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

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
            userDTOMapper.convertOrPostpone(src.getAddedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            userGroupRestDTOMapper.postpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.User2UserGroupMode.SHORT));
        }
    }

    public void beforeCollectionConversion(Collection<UserGroupInvolveActAsUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            Set<UUID> userIds = srcCollection.stream().map(UserGroupInvolveActAsUserEntity::getMachineUserId).collect(Collectors.toSet());
            Kit<UserEntity, UUID> users = userService.findEntities(userIds, EntitySmartService.ListFindMode.ifMissedLog, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.none);
            srcCollection.forEach(it -> it.setAddedByUser(users.get(it.getMachineUserId())));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            Set<UUID> userGroupIds = srcCollection.stream().map(UserGroupInvolveActAsUserEntity::getUserGroupId).collect(Collectors.toSet());
            Kit<UserGroupEntity, UUID> userGroupEntities = userGroupService.findEntities(userGroupIds, EntitySmartService.ListFindMode.ifMissedLog, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.none);
            srcCollection.forEach(it -> it.setUserGroup(userGroupEntities.get(it.getUserGroupId())));
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