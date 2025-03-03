package org.twins.core.mappers.rest.user;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.service.user.UserGroupService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {UserMode.class})
public class UserRestDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {
    private final UserGroupService userGroupService;

    @MapperModePointerBinding(modes = UserGroupMode.User2UserGroupMode.class)
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @Override
    public void map(UserEntity src, UserDTOv1 dst, MapperContext mapperContext) throws ServiceException {
        switch (mapperContext.getModeOrUse(UserMode.DETAILED)) {
            case SHORT -> dst
                    .setId(src.getId())
                    .setFullName(src.getName());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setFullName(src.getName())
                    .setEmail(src.getEmail())
                    .setAvatar(src.getAvatar());
        }
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            userGroupService.loadGroups(src);
            dst.setUserGroupIds(userGroupRestDTOMapper.postpone(src.getUserGroups(), mapperContext.forkOnPoint(UserGroupMode.User2UserGroupMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<UserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(UserGroupMode.User2UserGroupMode.HIDE)) {
            userGroupService.loadGroups(srcCollection);
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(UserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(UserEntity src) {
        return src.getId().toString();
    }

}
