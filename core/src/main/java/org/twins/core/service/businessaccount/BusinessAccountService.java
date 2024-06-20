package org.twins.core.service.businessaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAccountService {
    final BusinessAccountUserRepository businessAccountUserRepository;
    final BusinessAccountRepository businessAccountRepository;
    final EntitySmartService entitySmartService;
    @Lazy
    final TwinService twinService;
    final SystemEntityService systemEntityService;
    final UserService userService;
    @Lazy
    final AuthService authService;

    public BusinessAccountEntity findById(UUID businessAccountId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(businessAccountId, businessAccountRepository, findMode);
    }

    public UUID checkBusinessAccountId(UUID businessAccountId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(businessAccountId, businessAccountRepository, checkMode);
    }

    public void addUser(UUID businessAccountId, UUID userId, EntitySmartService.SaveMode businessAccountEntityCreateMode, EntitySmartService.SaveMode userCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        userService.addUser(userId, userCreateMode);
        addBusinessAccount(businessAccountId, businessAccountEntityCreateMode);
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

    public BusinessAccountEntity addBusinessAccount(UUID businessAccountId) throws ServiceException {
        return addBusinessAccount(businessAccountId, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public BusinessAccountEntity addBusinessAccount(UUID businessAccountId, EntitySmartService.SaveMode entityCreateMode) throws ServiceException {
        BusinessAccountEntity businessAccountEntity = new BusinessAccountEntity()
                .setId(businessAccountId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        EntitySmartService.SaveResult<BusinessAccountEntity> saveResult = entitySmartService.saveWithResult(businessAccountId, businessAccountEntity, businessAccountRepository, entityCreateMode);
        if (saveResult.isWasCreated()) {
            ApiUser apiUser = authService.getApiUser();
            twinService.duplicateTwin(systemEntityService.getTwinIdTemplateForBusinessAccount(), businessAccountEntity, apiUser.getUser(), businessAccountEntity.getId());
        }
        return saveResult.getSavedEntity();
    }

    public void updateBusinessAccount(BusinessAccountEntity businessAccountEntity) throws ServiceException {
        businessAccountRepository.save(businessAccountEntity);
    }

    public void deleteUser(UUID businessAccountId, UUID userId) throws ServiceException {
        BusinessAccountUserNoRelationProjection businessAccountUserEntity = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId, BusinessAccountUserNoRelationProjection.class);
        if (businessAccountUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "user[" + userId + "] is not registered in businessAccount[" + businessAccountId + "] ");
        entitySmartService.deleteAndLog(businessAccountUserEntity.id(), businessAccountUserRepository);
    }

    public Set<UUID> getValidBusinessAccountIdSetByTwinClass(TwinClassEntity twinClassEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<UUID> businessAccountIdList = null;
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN_BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT_USER:
                businessAccountIdList = businessAccountRepository.findBusinessAccountIdByUserIdAndDomainId(apiUser.getUser().getId(), apiUser.getDomain().getId());
                break;
            case USER:
                businessAccountIdList = businessAccountRepository.findBusinessAccountIdByUser(apiUser.getUser().getId());
                break;
            case DOMAIN_USER:
            case DOMAIN:
                businessAccountIdList = businessAccountRepository.findBusinessAccountIdByDomainId(apiUser.getDomain().getId());
                break;
        }
        return businessAccountIdList != null ? Set.copyOf(businessAccountIdList) : null;
    }
}
