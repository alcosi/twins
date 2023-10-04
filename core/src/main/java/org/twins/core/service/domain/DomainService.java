package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class DomainService {
    final FeaturerService featurerService;
    final UserService userService;
    final BusinessAccountService businessAccountService;
    final DomainRepository domainRepository;
    final DomainUserRepository domainUserRepository;
    final DomainBusinessAccountRepository domainBusinessAccountRepository;
    final EntitySmartService entitySmartService;
    @Lazy
    final PermissionService permissionService;
    final TwinClassService twinClassService;
    final TwinflowService twinflowService;

    public UUID checkDomainId(UUID domainId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(domainId, "domainId", domainRepository, checkMode);
    }

    public DomainEntity findDomain(UUID domainId, EntitySmartService.FindMode checkMode) throws ServiceException {
        return entitySmartService.findById(domainId, "domainId", domainRepository, checkMode);
    }


    public void addUser(UUID domainId, UUID userId, EntitySmartService.CreateMode userCreateMode) throws ServiceException {
        userService.addUser(userId, userCreateMode);
        if (domainUserRepository.findByDomainIdAndUserId(domainId, userId) != null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_ALREADY_EXISTS, "user[" + userId + "] is already registered in domain[" + domainId + "]");
        DomainUserEntity domainUserEntity = new DomainUserEntity()
                .domainId(domainId)
                .userId(userId)
                .createdAt(Timestamp.from(Instant.now()));
        domainUserRepository.save(domainUserEntity);
    }

    public void deleteUser(UUID domainId, UUID userId) throws ServiceException {
        DomainUserEntity domainUserEntity = domainUserRepository.findByDomainIdAndUserId(domainId, userId);
        if (domainUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "domain[" + domainId + "] user[" + userId + "] is not registered");
        domainUserRepository.deleteById(domainUserEntity.id());
    }

    public void addBusinessAccount(UUID domainId, UUID businessAccountId) throws ServiceException {
        addBusinessAccount(domainId, businessAccountId, false, EntitySmartService.CreateMode.ifNotPresentCreate);
    }

    public void addBusinessAccount(UUID domainId, UUID businessAccountId, boolean ignoreAlreadyExists, EntitySmartService.CreateMode businessAccountCreateMode) throws ServiceException {
        Optional<DomainEntity> domainEntity = domainRepository.findById(domainId);
        if (domainEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.DOMAIN_UNKNOWN, "unknown domain[" + domainId + "]");
        businessAccountService.addBusinessAccount(businessAccountId, businessAccountCreateMode);
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (domainBusinessAccountEntity != null)
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_ALREADY_EXISTS, "businessAccount[" + businessAccountId + "] is already registered in domain[" + domainId + "]");
        domainBusinessAccountEntity = new DomainBusinessAccountEntity()
                .setDomainId(domainId)
                .setBusinessAccountId(businessAccountId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        BusinessAccountInitiator businessAccountInitiator = featurerService.getFeaturer(domainEntity.get().getBusinessAccountInitiatorFeaturer(), BusinessAccountInitiator.class);
        businessAccountInitiator.init(domainEntity.get().getBusinessAccountInitiatorParams(), domainBusinessAccountEntity);
        domainBusinessAccountRepository.save(domainBusinessAccountEntity);
    }

    public void updateDomainBusinessAccount(DomainBusinessAccountEntity updateEntity) throws ServiceException {
        DomainBusinessAccountEntity dbEntity = getDomainBusinessAccountEntitySafe(updateEntity.getDomainId(), updateEntity.getBusinessAccountId());
        if (updateEntity.getPermissionSchemaId() != null) {
            dbEntity.setPermissionSchemaId(permissionService.checkPermissionSchemaAllowed(updateEntity.getDomainId(), updateEntity.getBusinessAccountId(), updateEntity.getPermissionSchemaId()));
        }
        if (updateEntity.getTwinClassSchemaId() != null) {
            dbEntity.setTwinClassSchemaId(twinClassService.checkTwinClassSchemaAllowed(updateEntity.getDomainId(), updateEntity.getTwinClassSchemaId()));
        }
        if (updateEntity.getTwinflowSchemaId() != null) {
            dbEntity.setTwinflowSchemaId(twinflowService.checkTwinflowSchemaAllowed(updateEntity.getDomainId(), updateEntity.getBusinessAccountId(), updateEntity.getTwinflowSchemaId()));
        }
        domainBusinessAccountRepository.save(dbEntity);
    }

    public DomainBusinessAccountEntity getDomainBusinessAccountEntitySafe(UUID domainId, UUID businessAccountId) throws ServiceException {
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (domainBusinessAccountEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_NOT_EXISTS, "businessAccount[" + businessAccountId + "] is not registered in domain[" + domainId + "]");
        return domainBusinessAccountEntity;
    }

    public void deleteBusinessAccount(UUID domainId, UUID businessAccountId) throws ServiceException {
        DomainBusinessAccountEntity domainBusinessAccountEntity = getDomainBusinessAccountEntitySafe(domainId, businessAccountId);
        domainBusinessAccountRepository.deleteById(domainBusinessAccountEntity.getId());
    }
}
