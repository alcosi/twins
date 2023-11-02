package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends EntitySecureFindServiceImpl<UserEntity> {
    final UserRepository userRepository;
    final EntitySmartService entitySmartService;

    @Override
    public CrudRepository<UserEntity, UUID> entityRepository() {
        return userRepository;
    }

    @Override
    public boolean isEntityReadDenied(UserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(UserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public UUID checkUserId(UUID userId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(userId, userRepository, checkMode);
    }

    public UserEntity findByUserId(UUID userId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(userId, userRepository, findMode);
    }

    public void addUser(UserEntity userEntity, EntitySmartService.SaveMode userSaveMode) throws ServiceException {
        userEntity.setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(userEntity.getId(), userEntity, userRepository, userSaveMode);
    }

    public void addUser(UUID userId, EntitySmartService.SaveMode userSaveMode) throws ServiceException {
        UserEntity userEntity = new UserEntity()
                .setId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        addUser(userEntity, userSaveMode);
    }

    public void updateUser(UserEntity updateEntity) throws ServiceException {
        UserEntity dbEntity = entitySmartService.findById(updateEntity.getId(), userRepository, EntitySmartService.FindMode.ifEmptyThrows);
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("name", dbEntity.getName(), updateEntity.getName()))
            dbEntity.setName(updateEntity.getName());
        if (changesHelper.isChanged("email", dbEntity.getEmail(), updateEntity.getEmail()))
            dbEntity.setEmail(updateEntity.getEmail());
        if (changesHelper.isChanged("avatar", dbEntity.getAvatar(), updateEntity.getAvatar()))
            dbEntity.setAvatar(updateEntity.getAvatar());
        if (changesHelper.hasChanges())
            entitySmartService.saveAndLogChanges(dbEntity, userRepository, changesHelper);
    }

    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //todo move to properties

}
