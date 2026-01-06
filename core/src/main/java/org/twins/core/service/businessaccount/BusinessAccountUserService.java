package org.twins.core.service.businessaccount;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserNoRelationProjection;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class BusinessAccountUserService extends EntitySecureFindServiceImpl<BusinessAccountUserEntity> {
    final BusinessAccountUserRepository businessAccountUserRepository;
    final EntitySmartService entitySmartService;
    @Lazy
    final BusinessAccountService businessAccountService;
    final UserService userService;
    @Lazy
    final AuthService authService;

    @Override
    public CrudRepository<BusinessAccountUserEntity, UUID> entityRepository() {
        return businessAccountUserRepository;
    }

    @Override
    public Function<BusinessAccountUserEntity, UUID> entityGetIdFunction() {
        return BusinessAccountUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(BusinessAccountUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(BusinessAccountUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void addUser(UUID businessAccountId, UUID userId, boolean ignoreAlreadyExists) throws ServiceException {
        BusinessAccountUserNoRelationProjection existed = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId, BusinessAccountUserNoRelationProjection.class);
        if (existed != null) {
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_USER_ALREADY_EXISTS, "user[" + userId + "] is already registered in businessAccount[" + businessAccountId + "]");
        }
        BusinessAccountUserEntity businessAccountUserEntity = new BusinessAccountUserEntity()
                .setBusinessAccountId(businessAccountId)
                .setUserId(userId);
        entitySmartService.save(businessAccountUserEntity, businessAccountUserRepository, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public void addUserSmart(UUID businessAccountId, UUID userId, EntitySmartService.SaveMode businessAccountEntityCreateMode, EntitySmartService.SaveMode userCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        userService.addUser(userId, userCreateMode);
        businessAccountService.addBusinessAccount(businessAccountId, null, businessAccountEntityCreateMode);
        addUser(businessAccountId, userId, ignoreAlreadyExists);
    }

    public void deleteUser(UUID businessAccountId, UUID userId) throws ServiceException {
        BusinessAccountUserNoRelationProjection businessAccountUserEntity = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId, BusinessAccountUserNoRelationProjection.class);
        if (businessAccountUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "user[" + userId + "] is not registered in businessAccount[" + businessAccountId + "] ");
        entitySmartService.deleteAndLog(businessAccountUserEntity.id(), businessAccountUserRepository);
    }

    public void loadBusinessAccounts(DomainUserEntity domainUser) {
        loadBusinessAccounts(Collections.singletonList(domainUser));
    }

    public void loadBusinessAccounts(Collection<DomainUserEntity> domainUserList) {
        if (CollectionUtils.isEmpty(domainUserList))
            return;
        Map<UUID, DomainUserEntity> needLoad = new HashMap<>();
        for (DomainUserEntity domainUser : domainUserList)
            if (domainUser.getBusinessAccountUserKit() == null)
                needLoad.put(domainUser.getUserId(), domainUser);
        if (needLoad.isEmpty())
            return;
        KitGrouped<BusinessAccountUserEntity, UUID, UUID> businessAccountUserKit = new KitGrouped<>(
                businessAccountUserRepository.findByUserIdIn(needLoad.keySet()), BusinessAccountUserEntity::getId, BusinessAccountUserEntity::getUserId);
        for (Map.Entry<UUID, DomainUserEntity> entry : needLoad.entrySet())
            entry.getValue().setBusinessAccountUserKit(new Kit<>(businessAccountUserKit.getGrouped(entry.getKey()), BusinessAccountUserEntity::getId));
    }
}
