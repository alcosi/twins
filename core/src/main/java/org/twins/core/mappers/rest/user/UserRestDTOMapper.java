package org.twins.core.mappers.rest.user;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@MapperModeBinding(modes = {MapperMode.UserMode.class})
public class UserRestDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {
    @Override
    public void map(UserEntity src, UserDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(MapperMode.UserMode.DETAILED)) {
            case SHORT:
                dst
                        .id(src.getId())
                        .fullName(src.getName());
                break;
            case DETAILED:
                dst
                        .id(src.getId())
                        .fullName(src.getName())
                        .email(src.getEmail())
                        .avatar(src.getAvatar());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.UserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(UserEntity src) {
        return src.getId().toString();
    }

}
