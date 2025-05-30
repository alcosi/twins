package org.twins.core.service.domain;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.dao.i18n.I18nLocaleRepository;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.*;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.domain.file.DomainFile;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.domain.twinoperation.TwinDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.featurer.domain.initiator.DomainInitiator;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.space.SpaceRoleService;
import org.twins.core.service.storage.StorageService;
import org.twins.core.service.twin.TwinAliasService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserGroup;
import org.twins.core.service.user.UserGroupService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.domain.DomainBusinessAccountSpecification.*;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class DomainService extends EntitySecureFindServiceImpl<DomainEntity> {
    private final FeaturerService featurerService;
    private final UserService userService;
    private final BusinessAccountService businessAccountService;
    private final DomainRepository domainRepository;
    private final DomainTypeRepository domainTypeRepository;
    private final DomainUserRepository domainUserRepository;
    private final DomainBusinessAccountRepository domainBusinessAccountRepository;
    private final EntitySmartService entitySmartService;
    private final ResourceService resourceService;
    private final StorageService storageService;
    @Lazy
    private final PermissionService permissionService;

    @Lazy
    private final AuthService authService;

    private final TwinClassService twinClassService;
    private final TwinflowService twinflowService;
    private final I18nLocaleRepository i18nLocaleRepository;
    private final DomainLocaleRepository domainLocaleRepository;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinAliasService twinAliasService;

    @Lazy
    private final SpaceRoleService spaceRoleService;

    @Lazy
    private final DataListService dataListService;

    @Lazy
    private final UserGroupService userGroupService;
    private final TierService tierService;
    private final DomainLocaleService domainLocaleService;


    @Override
    public CrudRepository<DomainEntity, UUID> entityRepository() {
        return domainRepository;
    }

    @Override
    public Function<DomainEntity, UUID> entityGetIdFunction() {
        return DomainEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DomainEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (authService.getApiUser().isDomainSpecified() && authService.getApiUser().getDomainId().equals(entity.getId()))
            return false;
        else if (!domainUserRepository.existsByDomainIdAndUserId(entity.getId(), authService.getApiUser().getUserId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logNormal() + " read is not allowed for current user[" + authService.getApiUser().getUserId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(DomainEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public Optional<DomainEntity> findByKey(String key) throws ServiceException {
        return Optional.ofNullable(domainRepository.findByKey(key));
    }

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

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public DomainEntity createDomain(DomainEntity domainEntity, DomainFile lightIcon, DomainFile darkIcon) throws ServiceException {
        if (StringUtils.isBlank(domainEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.DOMAIN_KEY_INCORRECT, "New domain key can not be blank");
        domainEntity.setKey(domainEntity.getKey().trim().replaceAll("\\s", "_").toLowerCase()); //todo replace all unsupported chars
        if (domainRepository.existsByKey(domainEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.DOMAIN_KEY_UNAVAILABLE);
        loadDomainType(domainEntity);
        domainEntity.setDomainStatusId(DomainStatus.ACTIVE);
        DomainInitiator domainInitiator = featurerService.getFeaturer(domainEntity.getDomainTypeEntity().getDomainInitiatorFeaturer(), DomainInitiator.class);
        domainEntity = domainInitiator.init(domainEntity);
        ApiUser apiUser = authService.getApiUser()
                .setDomainResolver(new DomainResolverGivenId(domainEntity.getId())); // to be sure
        domainLocaleService.addDomainLocale(domainEntity.getId(), apiUser.getLocale());
        addUser(domainEntity.getId(), apiUser.getUserId(), EntitySmartService.SaveMode.none, true);
        userGroupService.enterGroup(UserGroup.DOMAIN_ADMIN.uuid);
        return processIcons(domainEntity, lightIcon, darkIcon);
    }
    
    @Transactional(rollbackFor = Throwable.class)
    public DomainEntity updateDomain(DomainEntity updateEntity) throws ServiceException {
        DomainEntity dbEntity = findEntitySafe(authService.getApiUser().getDomainId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getName, DomainEntity::setName,
                DomainEntity.Fields.name, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getDescription, DomainEntity::setDescription,
                DomainEntity.Fields.description, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getBusinessAccountInitiatorFeaturerId, DomainEntity::setBusinessAccountInitiatorFeaturerId,
                DomainEntity.Fields.businessAccountInitiatorFeaturerId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getPermissionSchemaId, DomainEntity::setPermissionSchemaId,
                DomainEntity.Fields.permissionSchemaId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getTwinClassSchemaId, DomainEntity::setTwinClassSchemaId,
                DomainEntity.Fields.twinClassSchemaId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getBusinessAccountTemplateTwinId, DomainEntity::setBusinessAccountTemplateTwinId,
                DomainEntity.Fields.businessAccountTemplateTwinId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getDomainUserTemplateTwinId, DomainEntity::setDomainUserTemplateTwinId,
                DomainEntity.Fields.domainUserTemplateTwinId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getIconDarkResourceId, DomainEntity::setIconDarkResourceId,
                DomainEntity.Fields.iconDarkResourceId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getIconLightResourceId, DomainEntity::setIconLightResourceId,
                DomainEntity.Fields.iconLightResourceId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getDefaultTierId, DomainEntity::setDefaultTierId,
                DomainEntity.Fields.defaultTierId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getDefaultI18nLocaleId, DomainEntity::setDefaultI18nLocaleId,
                DomainEntity.Fields.defaultI18nLocaleId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getResourcesStorageId, DomainEntity::setResourcesStorageId,
                DomainEntity.Fields.resourcesStorageId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getAttachmentsStorageId, DomainEntity::setAttachmentsStorageId,
                DomainEntity.Fields.attachmentsStorageId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, DomainEntity::getNavbarFaceId, DomainEntity::setNavbarFaceId,
                DomainEntity.Fields.navbarFaceId, changesHelper);
        updateBusinessAccountInitiatorFeaturerId(dbEntity, updateEntity.getBusinessAccountInitiatorFeaturerId(), updateEntity.getBusinessAccountInitiatorParams(), changesHelper);
        updateUserGroupManagerFeaturerId(dbEntity, updateEntity.getUserGroupManagerFeaturerId(), updateEntity.getUserGroupManagerParams(), changesHelper);

        updateSafe(dbEntity, changesHelper);
        return dbEntity;
    }

    public void updateBusinessAccountInitiatorFeaturerId(DomainEntity dbDomainEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        FeaturerEntity newBusinessAccountInitiatorFeaturer = null;
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbDomainEntity.getBusinessAccountInitiatorFeaturerId(); // only params where changed
        }
        if (!MapUtils.areEqual(dbDomainEntity.getBusinessAccountInitiatorParams(), newFeaturerParams)) {
            newBusinessAccountInitiatorFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, BusinessAccountInitiator.class);
            changesHelper.add(DomainEntity.Fields.businessAccountInitiatorParams, dbDomainEntity.getBusinessAccountInitiatorParams(), newFeaturerParams);
            dbDomainEntity
                    .setBusinessAccountInitiatorParams(newFeaturerParams);
        }
        if (changesHelper.isChanged(DomainEntity.Fields.businessAccountInitiatorFeaturerId, dbDomainEntity.getBusinessAccountInitiatorFeaturerId(), newFeaturerId)) {
            if (newBusinessAccountInitiatorFeaturer == null)
                newBusinessAccountInitiatorFeaturer = featurerService.getFeaturerEntity(newFeaturerId);
            dbDomainEntity
                    .setBusinessAccountInitiatorFeaturerId(newBusinessAccountInitiatorFeaturer.getId())
                    .setBusinessAccountInitiatorFeaturer(newBusinessAccountInitiatorFeaturer);
        }
    }

    public void updateUserGroupManagerFeaturerId(DomainEntity dbDomainEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        FeaturerEntity newUserGroupManagerFeaturer = null;
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbDomainEntity.getUserGroupManagerFeaturerId(); // only params where changed
        }
        if (!MapUtils.areEqual(dbDomainEntity.getUserGroupManagerParams(), newFeaturerParams)) {
            newUserGroupManagerFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, UserGroupManager.class);
            changesHelper.add(DomainEntity.Fields.userGroupManagerParams, dbDomainEntity.getUserGroupManagerParams(), newFeaturerParams);
            dbDomainEntity
                    .setUserGroupManagerParams(newFeaturerParams);
        }
        if (changesHelper.isChanged(DomainEntity.Fields.userGroupManagerFeaturerId, dbDomainEntity.getUserGroupManagerFeaturerId(), newFeaturerId)) {
            if (newUserGroupManagerFeaturer == null)
                newUserGroupManagerFeaturer = featurerService.getFeaturerEntity(newFeaturerId);
            dbDomainEntity
                    .setUserGroupManagerFeaturerId(newUserGroupManagerFeaturer.getId())
                    .setUserGroupManagerFeaturer(newUserGroupManagerFeaturer);
        }
    }

    protected DomainEntity processIcons(DomainEntity domainEntity, DomainFile lightIcon, DomainFile darkIcon) throws ServiceException {
        var lightIconEntity = saveIconResourceIfExist(domainEntity, lightIcon);
        var darkIconEntity = saveIconResourceIfExist(domainEntity, darkIcon);
        if (lightIconEntity != null) {
            domainEntity.setIconLightResourceId(lightIconEntity.getId());
            domainEntity.setIconLightResource(lightIconEntity);
        }
        if (darkIconEntity != null) {
            domainEntity.setIconDarkResourceId(darkIconEntity.getId());
            domainEntity.setIconDarkResource(darkIconEntity);
        }
        if (darkIconEntity != null || lightIconEntity != null) {
            domainRepository.save(domainEntity);
        }
        return domainEntity;
    }

    private ResourceEntity saveIconResourceIfExist(DomainEntity domainEntity, DomainFile icon) throws ServiceException {
        if (icon != null) {
            return resourceService.addResource(icon.originalFileName(), icon.content());
        } else {
            return null;
        }
    }

    public PaginationResult<DomainEntity> findDomainListByUser(SimplePagination pagination) throws ServiceException {
        Page<DomainEntity> domainEntityList = domainUserRepository.findAllActiveDomainByUserId(authService.getApiUser().getUserId(), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(domainEntityList, pagination);
    }

    public void addUser(UUID domainId, UUID userId, EntitySmartService.SaveMode userCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        UserEntity user = userService.addUser(userId, userCreateMode);
        DomainUserNoRelationProjection existed = getDomainUserNoRelationProjection(domainId, userId, DomainUserNoRelationProjection.class);
        if (existed != null) {
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_ALREADY_EXISTS, "user[" + userId + "] is already registered in domain[" + domainId + "]");
        }
        Locale locale = authService.getApiUser().getLocale();
        checkLocaleActiveInDomain(locale);
        DomainUserEntity domainUserEntity = new DomainUserEntity()
                .setDomainId(domainId)
                .setUserId(userId)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setI18nLocaleId(locale);
        domainUserEntity = entitySmartService.save(domainUserEntity, domainUserRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        DomainEntity domain = authService.getApiUser().getDomain();
        if (domain.getDomainUserTemplateTwinId() != null) {
            TwinDuplicate duplicateTwin = twinService.createDuplicateTwin(domain.getDomainUserTemplateTwinId(), domainUserEntity.getId());
            duplicateTwin.getDuplicate().setHeadTwinId(userId);
            twinService.saveDuplicateTwin(duplicateTwin);
        }
    }

    public void deleteUser(UUID domainId, UUID userId) throws ServiceException {
        DomainUserNoRelationProjection domainUserEntity = getDomainUserNoRelationProjection(domainId, userId, DomainUserNoRelationProjection.class);
        if (domainUserEntity == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "domain[" + domainId + "] user[" + userId + "] is not registered");
        entitySmartService.deleteAndLog(domainUserEntity.id(), domainUserRepository);
    }

    public void addBusinessAccount(UUID domainId, UUID businessAccountId, UUID tierId, String name) throws ServiceException {
        addBusinessAccount(domainId, businessAccountId, tierId, name, EntitySmartService.SaveMode.ifNotPresentCreate, false);
    }

    public void addBusinessAccount(UUID domainId, UUID businessAccountId, UUID tierId, String name, EntitySmartService.SaveMode businessAccountCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        Optional<DomainEntity> domainEntity = domainRepository.findById(domainId);
        if (domainEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.DOMAIN_UNKNOWN, "unknown domain[" + domainId + "]");
        DomainEntity domain = domainEntity.get();
        if (domain.getDomainType() != DomainType.b2b)
            return; //only b2b domains support BA add
        BusinessAccountEntity businessAccountEntity = businessAccountService.addBusinessAccount(businessAccountId, name, businessAccountCreateMode);
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
        if (domainBusinessAccountEntity != null)
            if (ignoreAlreadyExists)
                return;
            else
                throw new ServiceException(ErrorCodeTwins.DOMAIN_BUSINESS_ACCOUNT_ALREADY_EXISTS, "businessAccount[" + businessAccountId + "] is already registered in domain[" + domainId + "]");
        domainBusinessAccountEntity = new DomainBusinessAccountEntity()
                .setDomainId(domainId)
                .setDomain(domain)
                .setBusinessAccountId(businessAccountId)
                .setBusinessAccount(businessAccountEntity)
                .setTierId(null == tierId ? domain.getDefaultTierId() : tierId)
                .setCreatedAt(Timestamp.from(Instant.now()));
        if (domainBusinessAccountEntity.getTierId() == null)
            throw new ServiceException(ErrorCodeTwins.TIER_NOT_CONFIGURED_FOR_DOMAIN, "Tier not configured for " + domain.logNormal());

        domainBusinessAccountEntity.setTier(tierService.findEntitySafe(domainBusinessAccountEntity.getTierId()));

        BusinessAccountInitiator businessAccountInitiator = featurerService.getFeaturer(domain.getBusinessAccountInitiatorFeaturer(), BusinessAccountInitiator.class);
        businessAccountInitiator.init(domain.getBusinessAccountInitiatorParams(), domainBusinessAccountEntity);
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
        Locale locale = Locale.forLanguageTag(localeName);
        checkLocaleActiveInDomain(locale);
        ApiUser apiUser = authService.getApiUser();
        domainUserRepository.updateLocale(apiUser.getDomainId(), apiUser.getUserId(), locale);
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

    private void checkLocaleActiveInDomain(Locale locale) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainLocaleEntity ret = domainLocaleRepository
                .findByDomainIdAndLocale(apiUser.getDomainId(), locale);
        if (ret == null) {
            throw new ServiceException(ErrorCodeTwins.DOMAIN_LOCALE_UNKNOWN);
        } else if (!ret.isActive()) {
            throw new ServiceException(ErrorCodeTwins.DOMAIN_LOCALE_INACTIVE, "locale is inactive in domain");
        } else if (!ret.getI18nLocale().isActive()) {
            throw new ServiceException(ErrorCodeTwins.DOMAIN_LOCALE_INACTIVE, "locale is inactive in system");
        }
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
        return where(
                checkUuid(DomainBusinessAccountEntity.Fields.domainId, domainId)
                        .and(checkBusinessAccountFieldLikeIn(BusinessAccountEntity.Fields.name, domainBusinessAccountSearch.getBusinessAccountNameLikeList(), false))
                        .and(checkBusinessAccountFieldNotLikeIn(BusinessAccountEntity.Fields.name, domainBusinessAccountSearch.getBusinessAccountNameNotLikeList(), true))
                        .and(checkUuidIn(domainBusinessAccountSearch.getPermissionSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.permissionSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getPermissionSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.permissionSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinflowSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinflowSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinflowSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinflowSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinClassSchemaIdList(), false, false, DomainBusinessAccountEntity.Fields.twinClassSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getTwinClassSchemaIdExcludeList(), true, true, DomainBusinessAccountEntity.Fields.twinClassSchemaId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getBusinessAccountIdList(), false, false, DomainBusinessAccountEntity.Fields.businessAccountId))
                        .and(checkUuidIn(domainBusinessAccountSearch.getBusinessAccountIdExcludeList(), true, false, DomainBusinessAccountEntity.Fields.businessAccountId))
        );
    }

    public AttachmentQuotas getTierQuotas() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!apiUser.isBusinessAccountSpecified())
            throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, "Business account not specified for " + apiUser.getUserId());
        DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(apiUser.getDomainId(), apiUser.getBusinessAccountId());
        AttachmentQuotas attachmentQuotas = new AttachmentQuotas();
        attachmentQuotas
                .setUsedCount(domainBusinessAccountEntity.getAttachmentsStorageUsedCount())
                .setUsedSize(domainBusinessAccountEntity.getAttachmentsStorageUsedSize())
                .setQuotaCount(Long.valueOf(domainBusinessAccountEntity.getTier().getAttachmentsStorageQuotaCount()))
                .setQuotaSize(domainBusinessAccountEntity.getTier().getAttachmentsStorageQuotaSize());
        return attachmentQuotas;
    }

    public AttachmentQuotas getDomainQuotas() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        DomainEntity domain = apiUser.getDomain();
        AttachmentQuotas attachmentQuotas = new AttachmentQuotas();
        attachmentQuotas
                .setUsedCount(domain.getAttachmentsStorageUsedCount())
                .setUsedSize(domain.getAttachmentsStorageUsedSize())
                //TODO quotas for domain level
                .setQuotaCount(0L)
                .setQuotaSize(0L);

        return attachmentQuotas;
    }

    public void loadIconResources(DomainEntity domain) throws ServiceException {
        loadIconResources(Collections.singletonList(domain));
    }

    public void loadIconResources(Collection<DomainEntity> domains) throws ServiceException {
        if (CollectionUtils.isEmpty(domains))
            return;
        Set<UUID> neadLoad = new HashSet<>();
        for (var domain : domains) {
            if (domain.getIconDarkResource() == null && domain.getIconDarkResourceId() != null)
                neadLoad.add(domain.getIconDarkResourceId());
            if (domain.getIconLightResource() == null && domain.getIconLightResourceId() != null)
                neadLoad.add(domain.getIconLightResourceId());
        }
        if (CollectionUtils.isEmpty(neadLoad))
            return;
        Kit<ResourceEntity, UUID> resources = resourceService.findEntitiesSafe(neadLoad);
        domains.forEach(domain -> {
            domain.setIconDarkResource(resources.get(domain.getIconDarkResourceId()));
            domain.setIconLightResource(resources.get(domain.getIconLightResourceId()));
        });
    }

    public void loadStorages(Collection<DomainEntity> domains) throws ServiceException {
        if (CollectionUtils.isEmpty(domains))
            return;
        Collection<UUID> resourceIdList = Streams.concat(
                        domains.stream().map(DomainEntity::getResourcesStorageId),
                        domains.stream().map(DomainEntity::getAttachmentsStorageId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, StorageEntity> storages = storageService
                .findEntities(resourceIdList, EntitySmartService.ListFindMode.ifMissedThrows, EntitySmartService.ReadPermissionCheckMode.none, EntitySmartService.EntityValidateMode.none)
                .stream()
                .collect(Collectors.toMap(StorageEntity::getId, e -> e));
        domains.forEach(domain -> {
            domain.setResourcesStorage(storages.get(domain.getResourcesStorageId()));
            domain.setAttachmentsStorage(storages.get(domain.getAttachmentsStorageId()));
        });
    }
}
