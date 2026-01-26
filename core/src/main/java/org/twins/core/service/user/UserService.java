package org.twins.core.service.user;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class UserService extends EntitySecureFindServiceImpl<UserEntity> {

    private final UserRepository userRepository;
    private final EntitySmartService entitySmartService;
    private final SystemEntityService systemEntityService;

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final AuthService authService;

    @Override
    public CrudRepository<UserEntity, UUID> entityRepository() {
        return userRepository;
    }

    @Override
    public Function<UserEntity, UUID> entityGetIdFunction() {
        return UserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo denied if user is not registered in current domain
        return false;
    }

    @Override
    public boolean validateEntity(UserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public UserEntity addUser(UserEntity userEntity, EntitySmartService.SaveMode userSaveMode) throws ServiceException {
        userEntity.setCreatedAt(Timestamp.from(Instant.now()));
        if (userEntity.getUserStatusId() == null) {
            userEntity.setUserStatusId(UserStatus.ACTIVE);
        }
        EntitySmartService.SaveResult<UserEntity> saveResult = entitySmartService.saveWithResult(userEntity.getId(), userEntity, userRepository, userSaveMode);
        // The logic of creating a user record in twin is implemented through a trigger in the database
        return saveResult.getSavedEntity();
    }

    public UserEntity addUser(UUID userId, EntitySmartService.SaveMode userSaveMode) throws ServiceException {
        return addUser(new UserEntity().setId(userId), userSaveMode);
    }

    public void updateUser(UserEntity updateEntity) throws ServiceException {
        UserEntity dbEntity = entitySmartService.findById(updateEntity.getId(), userRepository, EntitySmartService.FindMode.ifEmptyThrows);
        ChangesHelper changesHelper = new ChangesHelper();
        // The logic of updating fields[name, email, avatar] in twin and twin_field_simple is implemented through a trigger in the database
        if (changesHelper.isChanged(UserEntity.Fields.name, dbEntity.getName(), updateEntity.getName(), maskName(dbEntity.getName()), maskName(updateEntity.getName())))
            dbEntity.setName(updateEntity.getName());
        if (changesHelper.isChanged(UserEntity.Fields.email, dbEntity.getEmail(), updateEntity.getEmail(), maskEmail(dbEntity.getEmail()), maskEmail(updateEntity.getEmail())))
            dbEntity.setEmail(updateEntity.getEmail());
        if (changesHelper.isChanged(UserEntity.Fields.avatar, dbEntity.getAvatar(), updateEntity.getAvatar()))
            dbEntity.setAvatar(updateEntity.getAvatar());
        if (changesHelper.isChanged(UserEntity.Fields.userStatusId, dbEntity.getUserStatusId(), updateEntity.getUserStatusId()))
            dbEntity.setUserStatusId(updateEntity.getUserStatusId());
        updateSafe(dbEntity, changesHelper);
    }

    public void deleteUser(UUID userId) throws ServiceException {
        UserEntity userFromDB = findEntitySafe(userId);
        deleteUser(userFromDB);
    }

    public void deleteUser(UserEntity userEntity) throws ServiceException {
        userEntity
                .setUserStatusId(UserStatus.DELETED)
                .setName(maskName(userEntity.getName()))
                .setEmail(maskEmail(userEntity.getEmail()));
        userRepository.save(userEntity);
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

    public UserEntity loadUserAndCheck(UUID userId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UserEntity userEntity = null;
        if (apiUser.isDomainSpecified() && apiUser.isBusinessAccountSpecified()) {
            userEntity = userRepository.findUserByUserIdAndBusinessAccountIdAndDomainId(userId, apiUser.getBusinessAccountId(), apiUser.getDomainId());
            if (userEntity == null)
                throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in domain[" + apiUser.getDomainId() + "] or business account[" + apiUser.getBusinessAccountId() + "]");
        } else if (apiUser.isDomainSpecified()) {
            userEntity = userRepository.findUserByUserIdAndDomainId(userId, apiUser.getDomain().getId());
            if (userEntity == null)
                throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in domain[" + apiUser.getDomainId() + "]");
        } else if (apiUser.isBusinessAccountSpecified()) {
            userEntity = userRepository.findUserByUserIdAndBusinessAccountId(userId, apiUser.getBusinessAccount().getId());
            if (userEntity == null)
                throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in business account[" + apiUser.getBusinessAccountId() + "]");
        }
        if (userEntity == null)
            throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is unknown");
        return userEntity;
    }

    public static String maskName(String name) {
        if (name == null)
            return "";
        return maskData(name);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@"))
            return "";
        String[] parts = email.split("@");
        return maskData(parts[0]) + "@" + maskData(parts[1]);
    }

    public static String maskData(String data) {
        if (data.length() < 2)
            return data;
        return data.charAt(0) + "***" + data.charAt(data.length() - 1);
    }

    public boolean checkUserRegisteredInDomain(UUID userId, UUID domainId) {
        return userRepository.existsUserInDomain(userId, domainId);
    }

    public boolean checkUserRegisteredInBusinessAccount(UUID userId, UUID businessAccountId) {
        return userRepository.existsUserInBusinessAccount(userId, businessAccountId);
    }

    public boolean checkUserRegisteredInDomainAndBusinessAccount(UUID userId, UUID businessAccountId, UUID domainId) {
        return userRepository.existsUserInBusinessAccountAndInDomain(userId, businessAccountId, domainId);
    }

    public List<UUID> getUsersOutOfDomainAndBusinessAccount(Set<UUID> userIds, UUID businessAccountId, UUID domainId) {
        return userRepository.getUsersOutOfDomainAndBusinessAccount(userIds, businessAccountId, domainId);
    }

    public Set<UUID> filterUsersByBusinessAccountAndDomain(Collection<UUID> userIds, UUID businessAccountId, UUID domainId) {
        return userRepository.findUserIdByBusinessAccountIdAndDomainIdFiltered(businessAccountId, domainId, userIds);
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
