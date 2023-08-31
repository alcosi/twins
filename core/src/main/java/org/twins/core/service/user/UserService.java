package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    final UserRepository userRepository;

    public void addUser(UUID userId) throws ServiceException {
        UserEntity userEntity = new UserEntity()
                .id(userId);
        userRepository.save(userEntity);
    }

    public void updateUser(UUID userId, String name, String email, String avatar) throws ServiceException {
        UserEntity userEntity = new UserEntity().id(userId);
        if (StringUtils.isNoneEmpty(name))
            userEntity.name(name);
        if (StringUtils.isNoneEmpty(email))
            userEntity.email(email);
        if (StringUtils.isNoneEmpty(name))
            userEntity.avatar(avatar);
        userRepository.save(userEntity);
    }
}
