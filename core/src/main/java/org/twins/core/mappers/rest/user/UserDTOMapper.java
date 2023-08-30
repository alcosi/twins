package org.twins.core.mappers.rest.user;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class UserDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {
    @Override
    public void map(UserEntity src, UserDTOv1 dst, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case ID_ONLY:
                dst
                        .id(src.id());
                break;
            case DETAILED:
                dst
                        .id(src.id())
                        .name(src.name())
                        .email(src.email())
                        .avatar(src.avatar());
                break;
            default:
                dst
                        .id(src.id())
                        .name(src.name());
        }
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED;
    }
}
