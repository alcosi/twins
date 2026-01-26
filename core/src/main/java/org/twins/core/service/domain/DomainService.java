package org.twins.core.service.domain;

import com.google.common.collect.Streams;
import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.*;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.DomainResolverGivenId;
import org.twins.core.domain.attachment.AttachmentQuotas;
import org.twins.core.enums.domain.DomainStatus;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.featurer.domain.initiator.DomainInitiator;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.storage.StorageService;
import org.twins.core.service.user.UserGroup;
import org.twins.core.service.user.UserGroupService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class DomainService extends EntitySecureFindServiceImpl<DomainEntity> {
    private final FeaturerService featurerService;
    private final DomainRepository domainRepository;
    private final DomainTypeRepository domainTypeRepository;
    private final DomainUserRepository domainUserRepository;
    private final DomainBusinessAccountRepository domainBusinessAccountRepository;
    private final ResourceService resourceService;
    private final StorageService storageService;
    @Lazy
    private final AuthService authService;
    private final DomainLocaleRepository domainLocaleRepository;
    @Lazy
    private final DomainUserService domainUserService;
    @Lazy
    private final UserGroupService userGroupService;
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

    public Locale getDefaultDomainLocale(UUID domainId) {
        return domainRepository.findById(domainId, DomainLocaleProjection.class).defaultI18nLocaleId();
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public DomainEntity createDomain(DomainEntity domainEntity, FileData lightIcon, FileData darkIcon) throws ServiceException {
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
                .setDomainResolver(new DomainResolverGivenId(domainEntity.getId())); // welcome to new domain!
        domainLocaleService.addDomainLocale(domainEntity.getId(), apiUser.getLocale());
        domainUserService.addUser(apiUser.getUser(), false);
        userGroupService.enterGroup(UserGroup.DOMAIN_ADMIN.uuid);
        return processIcons(domainEntity, lightIcon, darkIcon);
    }

    @Transactional(rollbackFor = Throwable.class)
    public DomainEntity updateDomain(DomainEntity updateEntity, FileData lightIcon, FileData darkIcon) throws ServiceException {
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
        updateDomainIcons(dbEntity, lightIcon, darkIcon, changesHelper);

        updateSafe(dbEntity, changesHelper);
        return dbEntity;
    }

    public void updateDomainIcons(DomainEntity dbDomainEntity, FileData iconLight, FileData iconDark, ChangesHelper changesHelper) throws ServiceException {
        if (iconLight != null) {
            ResourceEntity newValue = saveIconResourceIfExist(iconLight);
            if (changesHelper.isChanged(DomainEntity.Fields.iconLightResourceId, dbDomainEntity.getIconLightResourceId(), newValue.getId())) {
                dbDomainEntity
                        .setIconLightResourceId(newValue.getId())
                        .setIconLightResource(newValue);
            }
        }
        if (iconDark != null) {
            ResourceEntity newValue = saveIconResourceIfExist(iconDark);
            if (changesHelper.isChanged(DomainEntity.Fields.iconDarkResourceId, dbDomainEntity.getIconDarkResourceId(), newValue.getId())) {
                dbDomainEntity
                        .setIconLightResourceId(newValue.getId())
                        .setIconLightResource(newValue);
            }
        }
    }

    public void updateBusinessAccountInitiatorFeaturerId(DomainEntity dbDomainEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbDomainEntity.getBusinessAccountInitiatorFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(DomainEntity.Fields.businessAccountInitiatorFeaturerId, dbDomainEntity.getBusinessAccountInitiatorFeaturerId(), newFeaturerId)) {
            FeaturerEntity newBusinessAccountInitiatorFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, BusinessAccountInitiator.class);
            dbDomainEntity
                    .setBusinessAccountInitiatorFeaturerId(newBusinessAccountInitiatorFeaturer.getId());
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbDomainEntity.getBusinessAccountInitiatorParams(), newFeaturerParams)) {
            changesHelper.add(DomainEntity.Fields.businessAccountInitiatorParams, dbDomainEntity.getBusinessAccountInitiatorParams(), newFeaturerParams);
            dbDomainEntity
                    .setBusinessAccountInitiatorParams(newFeaturerParams);
        }
    }

    public void updateUserGroupManagerFeaturerId(DomainEntity dbDomainEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbDomainEntity.getUserGroupManagerFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(DomainEntity.Fields.userGroupManagerFeaturerId, dbDomainEntity.getUserGroupManagerFeaturerId(), newFeaturerId)) {
            FeaturerEntity newUserGroupManagerFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, UserGroupManager.class);
            dbDomainEntity
                    .setUserGroupManagerFeaturerId(newUserGroupManagerFeaturer.getId())
                    .setUserGroupManagerFeaturer(newUserGroupManagerFeaturer);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbDomainEntity.getUserGroupManagerParams(), newFeaturerParams)) {
            changesHelper.add(DomainEntity.Fields.userGroupManagerParams, dbDomainEntity.getUserGroupManagerParams(), newFeaturerParams);
            dbDomainEntity
                    .setUserGroupManagerParams(newFeaturerParams);
        }
    }

    protected DomainEntity processIcons(DomainEntity domainEntity, FileData lightIcon, FileData darkIcon) throws ServiceException {
        var lightIconEntity = saveIconResourceIfExist(lightIcon);
        var darkIconEntity = saveIconResourceIfExist(darkIcon);
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

    private ResourceEntity saveIconResourceIfExist(FileData icon) throws ServiceException {
        if (icon != null) {
            return resourceService.addResource(icon.originalFileName(), icon.content());
        } else {
            return null;
        }
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

    public void checkLocaleActiveInDomain(Locale locale) throws ServiceException {
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

    public OwnerType checkDomainSupportedTwinClassOwnerType(DomainEntity domainEntity, OwnerType ownerType) throws ServiceException {
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
        Set<UUID> needLoad = new HashSet<>();
        for (var domain : domains) {
            if (domain.getIconDarkResource() == null && domain.getIconDarkResourceId() != null)
                needLoad.add(domain.getIconDarkResourceId());
            if (domain.getIconLightResource() == null && domain.getIconLightResourceId() != null)
                needLoad.add(domain.getIconLightResourceId());
        }
        if (CollectionUtils.isEmpty(needLoad))
            return;
        Kit<ResourceEntity, UUID> resources = resourceService.findEntitiesSafe(needLoad);
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

    public void loadUserGroupManager(DomainEntity src) {
        loadUserGroupManagers(Collections.singletonList(src));
    }

    public void loadUserGroupManagers(Collection<DomainEntity> srcCollection) {
        featurerService.loadFeaturers(srcCollection,
                DomainEntity::getId,
                DomainEntity::getUserGroupManagerFeaturerId,
                DomainEntity::getUserGroupManagerFeaturer,
                DomainEntity::setUserGroupManagerFeaturer);
    }
}
