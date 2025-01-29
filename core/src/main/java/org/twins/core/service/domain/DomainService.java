package org.twins.core.service.domain;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.dao.I18nLocaleRepository;
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
import org.twins.core.dao.specifications.locale.I18nLocaleSpecification;
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
    public static final UUID DEFAULT_RESOURCE_STORAGE_ID = UUID.fromString("0194a1cd-fc94-7c0b-9884-e3d45d2bebf3");

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
        return false;
    }

    @Override
    public boolean validateEntity(DomainEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
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
    public DomainEntity addDomain(DomainEntity domainEntity, DomainFile lightIcon, DomainFile darkIcon) throws ServiceException {
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
        return processIcons(domainEntity, lightIcon, darkIcon);
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
            domainEntity.setIconDarkResource(lightIconEntity);
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
        Page<DomainEntity> domainEntityList = domainUserRepository.findAllDomainByUserId(authService.getApiUser().getUserId(), PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(domainEntityList, pagination);
    }

    public void addUser(UUID domainId, UUID userId, EntitySmartService.SaveMode userCreateMode, boolean ignoreAlreadyExists) throws ServiceException {
        UserEntity user = userService.addUser(userId, userCreateMode);
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
            dbEntity.setTierId(tierService.checkTierAllowed(updateEntity.getTierId()));
        }
        if (!StringUtils.isEmpty(name) && changesHelper.isChanged(BusinessAccountEntity.Fields.name, dbEntity.getBusinessAccount().getName(), name)) {
            dbEntity.getBusinessAccount().setName(name);
            businessAccountService.updateBusinessAccount(dbEntity.getBusinessAccount());
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
