package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.permission.PermissionSchemaService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.space.SpaceRoleService;
import org.twins.core.service.twin.TwinAliasService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserGroupService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class DomainBusinessAccountService extends EntitySecureFindServiceImpl<DomainBusinessAccountEntity> {
    @Getter
    private final DomainBusinessAccountRepository domainBusinessAccountRepository;
    private final AuthService authService;
    private final FeaturerService featurerService;
    @Lazy
    private final TierService tierService;
    private final BusinessAccountService businessAccountService;
    @Lazy
    private final PermissionService permissionService;
    @Lazy
    private final PermissionSchemaService permissionSchemaService;
    private final TwinClassService twinClassService;
    private final TwinflowService twinflowService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinAliasService twinAliasService;
    @Lazy
    private final SpaceRoleService spaceRoleService;
    @Lazy
    private final DataListService dataListService;
    private final UserGroupService userGroupService;
    @Lazy
    private final HistoryService historyService;

    @Override
    public CrudRepository<DomainBusinessAccountEntity, UUID> entityRepository() {
        return domainBusinessAccountRepository;
    }

    @Override
    public Function<DomainBusinessAccountEntity, UUID> entityGetIdFunction() {
        return DomainBusinessAccountEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainBusinessAccountEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(DomainBusinessAccountEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DomainBusinessAccountEntity addBusinessAccount(BusinessAccountEntity businessAccount, UUID tierId, boolean ignoreAlreadyExists) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        if (domain.getDomainType() != DomainType.b2b)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_LEVEL_NOT_SUPPORTED, "no business account can be added to " + domain.logNormal());
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domain.getId(), businessAccount.getId());
        if (domainBusinessAccountEntity != null)
            if (ignoreAlreadyExists)
                return domainBusinessAccountEntity;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_ALREADY_EXISTS, businessAccount.logShort() + " is already registered in " + domain.logShort());
        domainBusinessAccountEntity = new DomainBusinessAccountEntity()
                .setDomainId(domain.getId())
                .setDomain(domain)
                .setBusinessAccountId(businessAccount.getId())
                .setBusinessAccount(businessAccount)
                .setTierId(null == tierId ? domain.getDefaultTierId() : tierId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        if (domainBusinessAccountEntity.getTierId() == null)
            throw new ServiceException(ErrorCodeTwins.TIER_NOT_CONFIGURED_FOR_DOMAIN, "Tier not configured for " + domain.logNormal());

        domainBusinessAccountEntity.setTier(tierService.findEntitySafe(domainBusinessAccountEntity.getTierId()));
        BusinessAccountInitiator businessAccountInitiator = featurerService.getFeaturer(domain.getBusinessAccountInitiatorFeaturerId(), BusinessAccountInitiator.class);
        businessAccountInitiator.init(domain.getBusinessAccountInitiatorParams(), domainBusinessAccountEntity);
        return domainBusinessAccountEntity;
    }

    public DomainBusinessAccountEntity addBusinessAccountSmart(UUID businessAccountId, UUID tierId, String name, EntitySmartService.SaveMode businessAccountCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        if (domain.getDomainType() != DomainType.b2b)
            return null; //only b2b domains support BA add
        BusinessAccountEntity businessAccount = businessAccountService.addBusinessAccount(businessAccountId, name, businessAccountCreateMode);
        return addBusinessAccount(businessAccount, tierId, ignoreAlreadyExists);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateDomainBusinessAccount(DomainBusinessAccountEntity updateEntity, String name) throws ServiceException {
        DomainBusinessAccountEntity dbEntity = getDomainBusinessAccountEntitySafe(updateEntity.getDomainId(), updateEntity.getBusinessAccountId());
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged(DomainBusinessAccountEntity.Fields.permissionSchemaId, dbEntity.getPermissionSchemaId(), updateEntity.getPermissionSchemaId())) {
            dbEntity.setPermissionSchemaId(permissionService.checkPermissionSchemaAllowed(updateEntity.getDomainId(), updateEntity.getBusinessAccountId(), updateEntity.getPermissionSchemaId()));
        }
        if (changesHelper.isChanged(DomainBusinessAccountEntity.Fields.twinClassSchemaId, dbEntity.getTwinClassSchemaId(), updateEntity.getTwinClassSchemaId())) {
            dbEntity.setTwinClassSchemaId(twinClassService.checkTwinClassSchemaAllowed(updateEntity.getDomainId(), updateEntity.getTwinClassSchemaId()));
        }
        if (changesHelper.isChanged(DomainBusinessAccountEntity.Fields.twinflowSchemaId, dbEntity.getTwinflowSchemaId(), updateEntity.getTwinflowSchemaId())) {
            dbEntity.setTwinflowSchemaId(twinflowService.checkTwinflowSchemaAllowed(updateEntity.getDomainId(), updateEntity.getBusinessAccountId(), updateEntity.getTwinflowSchemaId()));
        }
        if (null != updateEntity.getTierId() && changesHelper.isChanged(DomainBusinessAccountEntity.Fields.tierId, dbEntity.getTierId(), updateEntity.getTierId())) {
            dbEntity.setTierId(tierService.checkTierValidForRegistration(updateEntity.getTierId()));
        }
        if (!StringUtils.isEmpty(name) && changesHelper.isChanged(BusinessAccountEntity.Fields.name, dbEntity.getBusinessAccount().getName(), name)) {
            dbEntity.getBusinessAccount().setName(name);
            businessAccountService.updateBusinessAccount(dbEntity.getBusinessAccount());
        }
        if (changesHelper.hasChanges()) {
            dbEntity = domainBusinessAccountRepository.save(dbEntity);
            log.info("{} was updated: {}", dbEntity.logNormal(), changesHelper.collectForLog());
        }
    }

    public DomainBusinessAccountEntity getDomainBusinessAccountEntitySafe(UUID domainId, UUID businessAccountId) throws ServiceException {
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (domainBusinessAccountEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_NOT_EXISTS, "businessAccount[" + businessAccountId + "] is not registered in domain[" + domainId + "]");
        return domainBusinessAccountEntity;
    }

    @Transactional
    public void deleteBusinessAccountFromDomain(UUID businessAccountId) throws ServiceException {
        DomainBusinessAccountEntity domainBusinessAccountEntity = getDomainBusinessAccountEntitySafe(authService.getApiUser().getDomainId(), businessAccountId);
        twinService.forceDeleteTwins(businessAccountId);
        twinAliasService.forceDeleteAliasCounters(businessAccountId);
        userGroupService.processDomainBusinessAccountDeletion(businessAccountId);
        spaceRoleService.forceDeleteRoles(businessAccountId);
        dataListService.forceDeleteOptions(businessAccountId);
        twinflowService.forceDeleteSchemas(businessAccountId);
        permissionService.forceDeleteSchemas(businessAccountId);

        entitySmartService.deleteAndLog(domainBusinessAccountEntity.getId(), domainBusinessAccountRepository);
    }

    public PaginationResult<DomainBusinessAccountEntity> findDomainBusinessAccounts(DomainBusinessAccountSearch domainBusinessAccountSearch, SimplePagination pagination) throws ServiceException {
        if (domainBusinessAccountSearch == null)
            domainBusinessAccountSearch = new DomainBusinessAccountSearch(); //no filters
        Page<DomainBusinessAccountEntity> domainBusinessAccountsList = domainBusinessAccountRepository.findAll(createDomainBusinessAccountEntitySearchSpecification(domainBusinessAccountSearch), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(domainBusinessAccountsList, pagination);
    }

    public List<DomainBusinessAccountEntity> searchDomainBusinessAccounts(DomainBusinessAccountSearch domainBusinessAccountSearch) throws ServiceException {
        if (domainBusinessAccountSearch == null)
            domainBusinessAccountSearch = new DomainBusinessAccountSearch(); //no filters
        return domainBusinessAccountRepository.findAll(createDomainBusinessAccountEntitySearchSpecification(domainBusinessAccountSearch));
    }

    public Specification<DomainBusinessAccountEntity> createDomainBusinessAccountEntitySearchSpecification(DomainBusinessAccountSearch domainBusinessAccountSearch) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return checkUuid(DomainBusinessAccountEntity.Fields.domainId, domainId)
                        .and(checkBusinessAccountFieldLikeIn(BusinessAccountEntity.Fields.name, domainBusinessAccountSearch.getBusinessAccountNameLikeList(), false))
                        .and(checkBusinessAccountFieldNotLikeIn(BusinessAccountEntity.Fields.name, domainBusinessAccountSearch.getBusinessAccountNameNotLikeList(), true))
                        .and(checkUuidIn(domainBusinessAccountSearch.getPermissionSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.permissionSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getPermissionSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.permissionSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinflowSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinflowSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinflowSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinflowSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinClassSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinClassSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinClassSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinClassSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getBusinessAccountIdList(), false, false, DomainBusinessAccountEntity.Fields.businessAccountId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getBusinessAccountIdExcludeList(), true, false, DomainBusinessAccountEntity.Fields.businessAccountId));
    }
}
