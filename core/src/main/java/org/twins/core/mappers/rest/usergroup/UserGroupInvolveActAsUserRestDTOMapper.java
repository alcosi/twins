package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dto.rest.usergroup.UserGroupInvolveActAsUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupInvolveActAsUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.usergroup.UserGroupInvolveActAsUserService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = UserGroupInvolveActAsUserMode.class)
public class UserGroupInvolveActAsUserRestDTOMapper extends RestSimpleDTOMapper<UserGroupInvolveActAsUserEntity, UserGroupInvolveActAsUserDTOv1> {
    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = UserGroupMode.User2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final UserGroupInvolveActAsUserService userGroupInvolveActAsUserService;

    @Override
    public void map(UserGroupInvolveActAsUserEntity src, UserGroupInvolveActAsUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(UserMode.DETAILED)) {
            case SHORT -> dst
                    .setId(src.getId())
                    .setMachineUserId(src.getMachineUserId())
                    .setUserGroupId(src.getUserGroupId());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setMachineUserId(src.getMachineUserId())
                    .setUserGroupId(src.getUserGroupId())
                    .setAddedAt(src.getAddedAt().toLocalDateTime())
                    .setAddedByUserId(src.getAddedByUserId());
        }

        if (mapperContext.hasModeButNot(UserMode.UserGroupInvolveActAsUser2UserMode.HIDE)) {
            userGroupInvolveActAsUserService.loadAddedByUser(src);
            userGroupInvolveActAsUserService.loadMachineUser(src);
            userDTOMapper.convertOrPostpone(src.getAddedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.UserGroupInvolveActAsUser2UserMode.SHORT)));
            userDTOMapper.convertOrPostpone(src.getMachineUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.UserGroupInvolveActAsUser2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            userGroupInvolveActAsUserService.loadUserGroup(src);
            userGroupRestDTOMapper.postpone(src.getUserGroup(), mapperContext.forkOnPoint(UserGroupMode.UserGroupInvolveActAsUser2UserGroupMode.SHORT));
        }
    }

    public void beforeCollectionConversion(Collection<UserGroupInvolveActAsUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            userGroupInvolveActAsUserService.loadAddedByUser(srcCollection); //todo join into one load method
            userGroupInvolveActAsUserService.loadMachineUser(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            userGroupInvolveActAsUserService.loadUserGroup(srcCollection);
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(UserGroupInvolveActAsUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(UserGroupInvolveActAsUserEntity src) {
        return src.getId().toString();
    }
}