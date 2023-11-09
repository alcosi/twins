package org.twins.core.service.businessaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
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
    @Lazy
    final AuthService authService;

    public BusinessAccountEntity findById(UUID businessAccountId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(businessAccountId, businessAccountRepository, findMode);
    }

    public UUID checkBusinessAccountId(UUID businessAccountId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(businessAccountId, businessAccountRepository, checkMode);
    }

    public void addUser(UUID businessAccountId, UUID userId, EntitySmartService.SaveMode businessAccountEntityCreateMode) throws ServiceException {
        addBusinessAccount(businessAccountId, businessAccountEntityCreateMode);
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
        businessAccountEntity = entitySmartService.save(businessAccountId, businessAccountEntity, businessAccountRepository, entityCreateMode);
        if (EntitySmartService.SaveMode.ifNotPresentCreate == entityCreateMode
                || EntitySmartService.SaveMode.ifPresentThrowsElseCreate == entityCreateMode) {
            ApiUser apiUser = authService.getApiUser();
            twinService.duplicateTwin(systemEntityService.getTwinIdTemplateForBusinessAccount(), businessAccountEntity, apiUser.getUser(), businessAccountEntity.getId());
        }
        return businessAccountEntity;
    }

    public void updateBusinessAccount(BusinessAccountEntity businessAccountEntity) throws ServiceException {
        businessAccountRepository.save(businessAccountEntity);
    }

    public void deleteUser(UUID businessAccountId, UUID userId) throws ServiceException {
        BusinessAccountUserEntity businessAccountUserEntity = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId);
        if (businessAccountUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "user[" + userId + "] is not registered in businessAccount[" + businessAccountId + "] ");
        entitySmartService.deleteAndLog(businessAccountUserEntity.getId(), businessAccountRepository);
    }
}
