package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    final UserRepository userRepository;
    final EntitySmartService entitySmartService;

    public UUID checkUserId(UUID userId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(userId, "userId", userRepository, checkMode);
    }

    public UserEntity findByUserId(UUID userId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(userId, "userId", userRepository, findMode);
    }

    public void addUser(UserEntity userEntity, EntitySmartService.CreateMode userCreateMode) throws ServiceException {
        userEntity.setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.create(userEntity.getId(), userEntity, userRepository, userCreateMode);
    }

    public void addUser(UUID userId, EntitySmartService.CreateMode userCreateMode) throws ServiceException {
        UserEntity userEntity = new UserEntity()
                .setId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        addUser(userEntity, userCreateMode);
    }

    public void updateUser(UserEntity updateEntity) throws ServiceException {
        Optional<UserEntity> dbEntity = userRepository.findById(updateEntity.getId());
        if (dbEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "unknown user[" + updateEntity.getId() + "]");
        if (StringUtils.isNoneEmpty(updateEntity.getName()))
            dbEntity.get().setName(updateEntity.getName());
        if (StringUtils.isNoneEmpty(updateEntity.getEmail()))
            dbEntity.get().setEmail(updateEntity.getEmail());
        if (StringUtils.isNoneEmpty(updateEntity.getAvatar()))
            dbEntity.get().setAvatar(updateEntity.getAvatar());
        userRepository.save(dbEntity.get());
    }
}
