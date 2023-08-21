package org.twins.core.mappers.rest.user;

import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.mappers.rest.RestDTOConverter;
import org.twins.core.mappers.rest.RestDTOMapper;

@Component
public class UserDTOMapper implements RestDTOMapper<UserEntity, UserDTOv1> {
    public UserDTOv1 convert(UserEntity user) {
        UserDTOv1 userDTOv1 = new UserDTOv1();
        map(user, userDTOv1);
        return userDTOv1;
    }

    @Override
    public void map(UserEntity src, UserDTOv1 dst) {
        dst
                .id(src.getId())
                .name("John Doe")
                .email("some@mail.com")
                .avatar("http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
        ;
    }
}
