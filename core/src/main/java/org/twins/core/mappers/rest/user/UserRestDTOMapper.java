package org.twins.core.mappers.rest.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class UserRestDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {
    @Override
    public void map(UserEntity src, UserDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getName());
                break;
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .email(src.getEmail())
                        .avatar(src.getAvatar());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(UserEntity src) {
        return src.getId().toString();
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}
