package org.twins.core.mappers.rest.user;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class UserRestDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {
    @Override
    public void map(UserEntity src, UserDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case ID_ONLY:
                dst
                        .id(src.getId());
                break;
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .email(src.getEmail())
                        .avatar(src.getAvatar());
                break;
            default:
                dst
                        .id(src.getId())
                        .name(src.getName());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasMode(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(UserEntity src) {
        return src.getId().toString();
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED, HIDE;

        public static final String _HIDE = "HIDE";
        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
