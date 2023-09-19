package org.twins.core.service.businessaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;

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

    public BusinessAccountEntity findById(UUID businessAccountId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(businessAccountId, "businessAccountId", businessAccountRepository, findMode);
    }

    public UUID checkBusinessAccountId(UUID businessAccountId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(businessAccountId, "businessAccountId", businessAccountRepository, checkMode);
    }

    public void addUser(UUID businessAccountId, UUID userId, EntitySmartService.CreateMode businessAccountEntityMode) throws ServiceException {
        addBusinessAccount(businessAccountId);
        BusinessAccountUserEntity businessAccountEntity = new BusinessAccountUserEntity()
                .businessAccountId(businessAccountId)
                .userId(userId);
        businessAccountUserRepository.save(businessAccountEntity);
    }

    public void addBusinessAccount(UUID businessAccountId) throws ServiceException {
        addBusinessAccount(businessAccountId, EntitySmartService.CreateMode.createIgnoreExists);
    }

    public void addBusinessAccount(UUID businessAccountId, EntitySmartService.CreateMode entityCreateMode) throws ServiceException {
        BusinessAccountEntity businessAccountEntity = new BusinessAccountEntity()
                .setId(businessAccountId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.create(businessAccountId, businessAccountEntity, businessAccountRepository, entityCreateMode);
    }

    public void updateBusinessAccount(BusinessAccountEntity businessAccountEntity) throws ServiceException {
        businessAccountRepository.save(businessAccountEntity);
    }

    public void deleteUser(UUID businessAccountId, UUID userId) throws ServiceException {
        BusinessAccountUserEntity businessAccountUserEntity = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId);
        if (businessAccountUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "user[" + userId + "] is not registered in businessAccount[" + businessAccountId + "] ");
        businessAccountUserRepository.deleteById(businessAccountUserEntity.id());
    }
}
