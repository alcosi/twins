package org.twins.core.service.businessaccount;

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
import org.twins.core.dao.businessaccount.*;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAccountService extends EntitySecureFindServiceImpl<BusinessAccountEntity> {
    final BusinessAccountUserRepository businessAccountUserRepository;
    final BusinessAccountRepository businessAccountRepository;
    final EntitySmartService entitySmartService;
    @Lazy
    final TwinService twinService;
    final SystemEntityService systemEntityService;
    final UserService userService;
    @Lazy
    final AuthService authService;

    @Override
    public CrudRepository<BusinessAccountEntity, UUID> entityRepository() {
        return businessAccountRepository;
    }

    @Override
    public Function<BusinessAccountEntity, UUID> entityGetIdFunction() {
        return BusinessAccountEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(BusinessAccountEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(BusinessAccountEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public BusinessAccountEntity findById(UUID businessAccountId, EntitySmartService.FindMode findMode) throws ServiceException {
        return entitySmartService.findById(businessAccountId, businessAccountRepository, findMode);
    }

    public UUID checkBusinessAccountId(UUID businessAccountId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(businessAccountId, businessAccountRepository, checkMode);
    }

    public void addUser(UUID businessAccountId, UUID userId, EntitySmartService.SaveMode businessAccountEntityCreateMode, EntitySmartService.SaveMode userCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        userService.addUser(userId, userCreateMode);
        addBusinessAccount(businessAccountId, null, businessAccountEntityCreateMode);
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

    public BusinessAccountEntity addBusinessAccount(UUID businessAccountId, String name) throws ServiceException {
        return addBusinessAccount(businessAccountId, name, EntitySmartService.SaveMode.saveAndLogOnException);
    }

    public BusinessAccountEntity addBusinessAccount(UUID businessAccountId, String name, EntitySmartService.SaveMode entityCreateMode) throws ServiceException {
        BusinessAccountEntity businessAccountEntity = new BusinessAccountEntity()
                .setId(businessAccountId)
                .setName(name)
                .setCreatedAt(Timestamp.from(Instant.now()));
        EntitySmartService.SaveResult<BusinessAccountEntity> saveResult = entitySmartService.saveWithResult(businessAccountId, businessAccountEntity, businessAccountRepository, entityCreateMode);
        if (saveResult.isWasCreated()) {
            twinService.duplicateTwin(systemEntityService.getTwinIdTemplateForBusinessAccount(), businessAccountEntity.getId());
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
