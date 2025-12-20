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
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.domain.apiuser.BusinessAccountResolverGivenId;
import org.twins.core.domain.twinoperation.TwinDuplicate;
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
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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

    @Transactional(rollbackFor = Throwable.class)
    public BusinessAccountEntity addBusinessAccount(UUID businessAccountId, String name, EntitySmartService.SaveMode entityCreateMode) throws ServiceException {
        BusinessAccountEntity businessAccountEntity = new BusinessAccountEntity()
                .setId(businessAccountId)
                .setName(name)
                .setCreatedAt(Timestamp.from(Instant.now()));
        EntitySmartService.SaveResult<BusinessAccountEntity> saveResult = entitySmartService.saveWithResult(businessAccountId, businessAccountEntity, businessAccountRepository, entityCreateMode);
        if (saveResult.isWasCreated()) {
            if (!authService.getApiUser().isBusinessAccountSpecified()) {
                authService.getApiUser()
                        .setBusinessAccountResolver(new BusinessAccountResolverGivenId(businessAccountId)) // welcome to new BA
                        .setCheckMembershipMode(false); // BA is just created, so no sense to check BA - User membership
            }
            TwinDuplicate twinDuplicate = twinService.createDuplicateTwin(systemEntityService.getTwinIdTemplateForBusinessAccount(), businessAccountEntity.getId());
            twinService.saveDuplicateTwin(twinDuplicate);
        }
        return saveResult.getSavedEntity();
    }

    public void updateBusinessAccount(BusinessAccountEntity businessAccountEntity) throws ServiceException {
        businessAccountRepository.save(businessAccountEntity);
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
