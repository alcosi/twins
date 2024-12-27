package org.twins.core.mappers.rest.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {UserMode.class})
public class UserRestDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {
    @Override
    public void map(UserEntity src, UserDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(UserMode.DETAILED)) {
            case SHORT:
                dst
                        .setId(src.getId())
                        .setFullName(src.getName());
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setFullName(src.getName())
                        .setEmail(src.getEmail())
                        .setAvatar(src.getAvatar());
                break;
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
