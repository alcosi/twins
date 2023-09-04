package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.exception.ErrorCodeTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    final UserRepository userRepository;

    public void addUser(UUID userId) throws ServiceException {
        UserEntity userEntity = new UserEntity()
                .id(userId)
                .createdAt(Timestamp.from(Instant.now()));
        userRepository.save(userEntity);
    }

    public void updateUser(UserEntity updateEntity) throws ServiceException {
        Optional<UserEntity> dbEntity = userRepository.findById(updateEntity.id());
        if (dbEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "unknown user[" + updateEntity.id() + "]");
        if (StringUtils.isNoneEmpty(updateEntity.name()))
            dbEntity.get().name(updateEntity.name());
        if (StringUtils.isNoneEmpty(updateEntity.email()))
            dbEntity.get().email(updateEntity.email());
        if (StringUtils.isNoneEmpty(updateEntity.avatar()))
            dbEntity.get().avatar(updateEntity.avatar());
        userRepository.save(dbEntity.get());
    }
}
