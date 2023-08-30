package org.twins.core.mappers.rest.user;

import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.RestDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
public class UserDTOMapper extends RestSimpleDTOMapper<UserEntity, UserDTOv1> {

    @Override
    public void map(UserEntity src, UserDTOv1 dst) {
        dst
                .id(src.id())
                .name("John Doe")
                .email("some@mail.com")
                .avatar("http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
        ;
    }
}
