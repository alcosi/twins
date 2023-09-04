package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.UUIDCheckService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAccountService {
    final BusinessAccountUserRepository businessAccountUserRepository;
    final BusinessAccountRepository businessAccountRepository;
    final UUIDCheckService uuidCheckService;

    public UUID checkBusinessAccountId(String businessAccountId, UUIDCheckService.CheckMode checkMode) throws ServiceException {
        return uuidCheckService.check(businessAccountId, "businessAccountId", businessAccountRepository, checkMode);
    }

    public UUID checkBusinessAccountId(UUID businessAccountId, UUIDCheckService.CheckMode checkMode) throws ServiceException {
        return uuidCheckService.check(businessAccountId, "businessAccountId", businessAccountRepository, checkMode);
    }

    public void addUser(UUID businessAccountId, UUID userId) throws ServiceException {
        BusinessAccountUserEntity businessAccountEntity = new BusinessAccountUserEntity()
                .businessAccountId(businessAccountId)
                .userId(userId);
        businessAccountUserRepository.save(businessAccountEntity);
    }


    public void addBusinessAccount(UUID businessAccountId) throws ServiceException {
        BusinessAccountEntity businessAccountEntity = new BusinessAccountEntity()
                .id(businessAccountId)
                .createdAt(Timestamp.from(Instant.now()));
        businessAccountRepository.save(businessAccountEntity);
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
