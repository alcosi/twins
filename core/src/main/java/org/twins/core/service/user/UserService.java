package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class UserService extends EntitySecureFindServiceImpl<UserEntity> {
    final UserRepository userRepository;
    final EntitySmartService entitySmartService;
    @Lazy
    final TwinService twinService;
    final SystemEntityService systemEntityService;
    @Lazy
    final AuthService authService;

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


    public UserEntity addUser(UserEntity userEntity, EntitySmartService.SaveMode userSaveMode) throws ServiceException {
        userEntity.setCreatedAt(Timestamp.from(Instant.now()));
        EntitySmartService.SaveResult<UserEntity> saveResult = entitySmartService.saveWithResult(userEntity.getId(), userEntity, userRepository, userSaveMode);
        if (saveResult.isWasCreated())
            twinService.duplicateTwin(systemEntityService.getTwinIdTemplateForUser(), null, userEntity, userEntity.getId());
        return saveResult.getSavedEntity();
    }

    public UserEntity addUser(UUID userId, EntitySmartService.SaveMode userSaveMode) throws ServiceException {
        UserEntity userEntity = new UserEntity()
                .setId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        return addUser(userEntity, userSaveMode);
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


    public Set<UUID> getValidUserIdSetByTwinClass(TwinClassEntity twinClassEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UUID> userIdList = null;
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN_BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT_USER:
                // only users linked to domain and businessAccount at once will be selected
                userIdList = userRepository.findUserIdByBusinessAccountIdAndDomainId(apiUser.getBusinessAccount().getId(), apiUser.getDomain().getId());
                break;
            case BUSINESS_ACCOUNT:
                // only users linked to businessAccount will be selected
                userIdList = userRepository.findUserIdByBusinessAccountId(apiUser.getBusinessAccount().getId());
                break;
            case DOMAIN_USER:
            case DOMAIN:
                // only users linked to domain will be selected
                userIdList = userRepository.findUserIdByDomainId(apiUser.getDomain().getId());
                break;
            case USER:
                //todo all users
        }
        return userIdList != null ? Set.copyOf(userIdList) : null;
    }
}
