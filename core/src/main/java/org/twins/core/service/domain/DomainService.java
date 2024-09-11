package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.dao.I18nLocaleRepository;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.*;
import org.twins.core.dao.specifications.locale.I18nLocaleSpecification;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.featurer.domain.initiator.DomainInitiator;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.space.SpaceRoleService;
import org.twins.core.service.twin.TwinAliasService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class DomainService {
    final FeaturerService featurerService;
    final UserService userService;
    final BusinessAccountService businessAccountService;
    final DomainRepository domainRepository;
    final DomainTypeRepository domainTypeRepository;
    final DomainUserRepository domainUserRepository;
    final DomainBusinessAccountRepository domainBusinessAccountRepository;
    final EntitySmartService entitySmartService;

    @Lazy
    final PermissionService permissionService;

    @Lazy
    final AuthService authService;

    final TwinClassService twinClassService;
    final TwinflowService twinflowService;
    final SystemEntityService systemEntityService;
    final I18nLocaleRepository i18nLocaleRepository;
    final DomainLocaleRepository domainLocaleRepository;
    @Lazy
    final TwinService twinService;
    @Lazy
    final TwinAliasService twinAliasService;

    @Lazy
    final SpaceRoleService spaceRoleService;

    @Lazy
    final DataListService dataListService;

    @Lazy
    final UserGroupService userGroupService;

    public UUID checkDomainId(UUID domainId, EntitySmartService.CheckMode checkMode) throws ServiceException {
        return entitySmartService.check(domainId, domainRepository, checkMode);
    }

    public DomainUserEntity getDomainUser() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return domainUserRepository.findByDomainIdAndUserId(apiUser.getDomainId(), apiUser.getUserId());
    }

    public Locale getDefaultDomainLocale(UUID domainId) {
        return domainRepository.findById(domainId, DomainLocaleProjection.class).defaultI18nLocaleId();
    }

    public DomainUserNoRelationProjection getDomainUserNoRelationProjection(UUID domainId, UUID userId, Class<DomainUserNoRelationProjection> clazz) throws ServiceException {
        return domainUserRepository.findByDomainIdAndUserId(domainId, userId, clazz);
    }

    @Transactional
    public DomainEntity addDomain(DomainEntity domainEntity) throws ServiceException {
        if (StringUtils.isBlank(domainEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.DOMAIN_KEY_INCORRECT, "New domain key can not be blank");
        domainEntity.setKey(domainEntity.getKey().trim().replaceAll("\\s", "_").toLowerCase()); //todo replace all unsupported chars
        if (domainRepository.existsByKey(domainEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.DOMAIN_KEY_UNAVAILABLE);
        loadDomainType(domainEntity);
        DomainInitiator domainInitiator = featurerService.getFeaturer(domainEntity.getDomainTypeEntity().getDomainInitiatorFeaturer(), DomainInitiator.class);
        domainEntity = domainInitiator.init(domainEntity);
        ApiUser apiUser = authService.getApiUser()
                .setDomainResolver(new DomainResolverGivenId(domainEntity.getId())); // to be sure
        addUser(domainEntity.getId(), apiUser.getUserId(), EntitySmartService.SaveMode.none, true);
        return domainEntity;
    }

    public PaginationResult<DomainEntity> findDomainListByUser(SimplePagination pagination) throws ServiceException {
        Page<DomainEntity> domainEntityList = domainUserRepository.findAllDomainByUserId(authService.getApiUser().getUserId(), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(domainEntityList, pagination);
    }

    public void addUser(UUID domainId, UUID userId, EntitySmartService.SaveMode userCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        userService.addUser(userId, userCreateMode);
        DomainUserNoRelationProjection existed = getDomainUserNoRelationProjection(domainId, userId, DomainUserNoRelationProjection.class);
        if (existed != null)
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_ALREADY_EXISTS, "user[" + userId + "] is already registered in domain[" + domainId + "]");
        DomainUserEntity domainUserEntity = new DomainUserEntity()
                .setDomainId(domainId)
                .setUserId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(domainUserEntity, domainUserRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    public void deleteUser(UUID domainId, UUID userId) throws ServiceException {
        DomainUserNoRelationProjection domainUserEntity = getDomainUserNoRelationProjection(domainId, userId, DomainUserNoRelationProjection.class);
        if (domainUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "domain[" + domainId + "] user[" + userId + "] is not registered");
        entitySmartService.deleteAndLog(domainUserEntity.id(), domainUserRepository);
    }

    public void addBusinessAccount(UUID domainId, UUID businessAccountId) throws ServiceException {
        addBusinessAccount(domainId, businessAccountId, EntitySmartService.SaveMode.ifNotPresentCreate, false);
    }

    public void addBusinessAccount(UUID domainId, UUID businessAccountId, EntitySmartService.SaveMode businessAccountCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        Optional<DomainEntity> domainEntity = domainRepository.findById(domainId);
        if (domainEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.DOMAIN_UNKNOWN, "unknown domain[" + domainId + "]");
        BusinessAccountEntity businessAccountEntity = businessAccountService.addBusinessAccount(businessAccountId, businessAccountCreateMode);
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (domainBusinessAccountEntity != null)
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_ALREADY_EXISTS, "businessAccount[" + businessAccountId + "] is already registered in domain[" + domainId + "]");
        domainBusinessAccountEntity = new DomainBusinessAccountEntity()
                .setDomainId(domainId)
                .setDomain(domainEntity.get())
                .setBusinessAccountId(businessAccountId)
                .setBusinessAccount(businessAccountEntity)
                .setCreatedAt(Timestamp.from(Instant.now()));
        BusinessAccountInitiator businessAccountInitiator = featurerService.getFeaturer(domainEntity.get().getBusinessAccountInitiatorFeaturer(), BusinessAccountInitiator.class);
        businessAccountInitiator.init(domainEntity.get().getBusinessAccountInitiatorParams(), domainBusinessAccountEntity);
    }

    public void updateDomainBusinessAccount(DomainBusinessAccountEntity updateEntity) throws ServiceException {
        DomainBusinessAccountEntity dbEntity = getDomainBusinessAccountEntitySafe(updateEntity.getDomainId(), updateEntity.getBusinessAccountId());
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("permissionSchema", dbEntity.getPermissionSchemaId(), updateEntity.getPermissionSchemaId())) {
            dbEntity.setPermissionSchemaId(permissionService.checkPermissionSchemaAllowed(updateEntity.getDomainId(), updateEntity.getBusinessAccountId(), updateEntity.getPermissionSchemaId()));
        }
        if (changesHelper.isChanged("twinClassSchema", dbEntity.getTwinClassSchemaId(), updateEntity.getTwinClassSchemaId())) {
            dbEntity.setTwinClassSchemaId(twinClassService.checkTwinClassSchemaAllowed(updateEntity.getDomainId(), updateEntity.getTwinClassSchemaId()));
        }
        if (changesHelper.isChanged("twinflowSchema", dbEntity.getTwinflowSchemaId(), updateEntity.getTwinflowSchemaId())) {
            dbEntity.setTwinflowSchemaId(twinflowService.checkTwinflowSchemaAllowed(updateEntity.getDomainId(), updateEntity.getBusinessAccountId(), updateEntity.getTwinflowSchemaId()));
        }
        if (changesHelper.hasChanges()) {
            dbEntity = domainBusinessAccountRepository.save(dbEntity);
            log.info(dbEntity.easyLog(EasyLoggable.Level.NORMAL) + " was updated: " + changesHelper.collectForLog());
        }
    }

    public DomainBusinessAccountEntity getDomainBusinessAccountEntitySafe(UUID domainId, UUID businessAccountId) throws ServiceException {
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (domainBusinessAccountEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_NOT_EXISTS, "businessAccount[" + businessAccountId + "] is not registered in domain[" + domainId + "]");
        return domainBusinessAccountEntity;
    }

    @Transactional
    public void deleteBusinessAccountFromDomain(UUID domainId, UUID businessAccountId) throws ServiceException {
        DomainBusinessAccountEntity domainBusinessAccountEntity = getDomainBusinessAccountEntitySafe(domainId, businessAccountId);

        twinService.forceDeleteTwins(businessAccountId);
        twinAliasService.forceDeleteAliasCounters(businessAccountId);
        userGroupService.processDomainBusinessAccountDeletion(businessAccountId);
        spaceRoleService.forceDeleteRoles(businessAccountId);
        dataListService.forceDeleteOptions(businessAccountId);
        twinflowService.forceDeleteSchemas(businessAccountId);
        permissionService.forceDeleteSchemas(businessAccountId);

        entitySmartService.deleteAndLog(domainBusinessAccountEntity.getId(), domainBusinessAccountRepository);
    }

    @Transactional
    public void updateLocaleByDomainUser(String localeName) throws ServiceException {
        if (!i18nLocaleRepository.exists(I18nLocaleSpecification.checkLocale(localeName)))
            throw new ServiceException(ErrorCodeTwins.DOMAIN_LOCALE_UNKNOWN, "unknown locale [" + localeName + "]");
        ApiUser apiUser = authService.getApiUser();
        domainUserRepository.updateLocale(apiUser.getDomainId(), apiUser.getUserId(), Locale.forLanguageTag(localeName));
    }

    public List<DomainLocaleEntity> getLocaleList() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<DomainLocaleEntity> domainLocales = domainLocaleRepository.findByDomainIdAndActiveTrueAndI18nLocaleActiveTrue(apiUser.getDomainId());
        return domainLocales.stream()
                .map(el -> {
                    if (StringUtils.isEmpty(el.getIcon()))
                        el.setIcon(el.getI18nLocale().getIcon());
                    return el;
                })
                .collect(Collectors.toList());
    }

    public DomainTypeEntity loadDomainType(DomainEntity domainEntity) throws ServiceException {
        if (domainEntity.getDomainTypeEntity() != null)
            return domainEntity.getDomainTypeEntity();
        domainEntity.setDomainTypeEntity(
                domainTypeRepository.findById(domainEntity.getDomainType().getId()).orElseThrow(() -> new ServiceException(ErrorCodeTwins.DOMAIN_TYPE_UNSUPPORTED)));
        return domainEntity.getDomainTypeEntity();
    }

    public TwinClassEntity.OwnerType checkDomainSupportedTwinClassOwnerType(DomainEntity domainEntity, TwinClassEntity.OwnerType ownerType) throws ServiceException {
        DomainTypeEntity domainTypeEntity = loadDomainType(domainEntity);
        DomainInitiator domainInitiator = featurerService.getFeaturer(domainTypeEntity.getDomainInitiatorFeaturer(), DomainInitiator.class);
        if (ownerType != null) {
            if (!domainInitiator.isSupportedTwinClassOwnerType(ownerType)) {
                log.warn(domainEntity.logNormal() + " Unsupported ownerType[" + ownerType + "]. Using default");
                return domainInitiator.getDefaultTwinClassOwnerType();
            } else
                return ownerType;
        } else
            return domainInitiator.getDefaultTwinClassOwnerType();
    }
}
