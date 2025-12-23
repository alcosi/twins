package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CacheUtils;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;


@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinStatusService extends EntitySecureFindServiceImpl<TwinStatusEntity> {
    final TwinStatusRepository twinStatusRepository;
    final TwinClassService twinClassService;
    final I18nService i18nService;

    private final ResourceService resourceService;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public CrudRepository<TwinStatusEntity, UUID> entityRepository() {
        return twinStatusRepository;
    }

    @Override
    public Function<TwinStatusEntity, UUID> entityGetIdFunction() {
        return TwinStatusEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatusEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatusEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (null == entity.getTwinClassId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_STATUS_TWIN_CLASS_NOT_SPECIFIED.getMessage());
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getType() == null) {
                    entity.setType(StatusType.BASIC);
                }
                //todo validate that status is uniq in class
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
        }
        return true;
    }

    @Override
    public CacheSupportType getCacheSupportType() {
        return CacheSupportType.REQUEST;
    }

    public Kit<TwinStatusEntity, UUID> loadStatusesForTwinClasses(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getTwinStatusKit() != null)
            return twinClassEntity.getTwinStatusKit();
        twinClassEntity.setTwinStatusKit(new Kit<>(twinStatusRepository.findByTwinClassIdIn(twinClassEntity.getExtendedClassIdSet()), TwinStatusEntity::getId));
        return twinClassEntity.getTwinStatusKit();
    }

    public void loadStatusesForTwinClasses(Collection<TwinClassEntity> twinClassEntities) {
        Map<UUID, TwinClassEntity> needLoad = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassEntities)
            if (twinClassEntity.getTwinStatusKit() == null)
                needLoad.put(twinClassEntity.getId(), twinClassEntity);
        if (needLoad.isEmpty())
            return;
        Set<UUID> allClassesSet = new HashSet<>();
        for (TwinClassEntity twinClassEntity : needLoad.values())
            if (twinClassEntity.getExtendedClassIdSet() != null)
                allClassesSet.addAll(twinClassEntity.getExtendedClassIdSet());
        List<TwinStatusEntity> twinStatusEntityList = twinStatusRepository.findByTwinClassIdIn(allClassesSet);
        if (CollectionUtils.isEmpty(twinStatusEntityList))
            return;
        Map<UUID, List<TwinStatusEntity>> statussMap = new HashMap<>(); // key - twinClassId
        for (TwinStatusEntity twinStatusEntity : twinStatusEntityList) { // grouping by twinClassId
            statussMap.computeIfAbsent(twinStatusEntity.getTwinClassId(), k -> new ArrayList<>());
            statussMap.get(twinStatusEntity.getTwinClassId()).add(twinStatusEntity);
        }
        TwinClassEntity twinClassEntity;
        List<TwinStatusEntity> statusList;
        for (Map.Entry<UUID, TwinClassEntity> entry : needLoad.entrySet()) {
            twinClassEntity = entry.getValue();
            statusList = new ArrayList<>();
            if (twinClassEntity.getExtendedClassIdSet() == null) { // it's strange, because in the simplest case class will have link to itself
                if (statussMap.containsKey(twinClassEntity.getId()))
                    statusList.addAll(statussMap.get(twinClassEntity.getId()));
            } else {
                for (UUID twinClassId : twinClassEntity.getExtendedClassIdSet()) {
                    if (statussMap.containsKey(twinClassId))
                        statusList.addAll(statussMap.get(twinClassId));
                }
            }
            twinClassEntity.setTwinStatusKit(new Kit<>(statusList, TwinStatusEntity::getId));
        }
    }

    public boolean checkStatusAllowed(TwinEntity twinEntity, TwinStatusEntity twinStatusEntity) {
        if (twinStatusEntity.getTwinClassId() == twinEntity.getTwinClassId()) {
            return true;
        }
        return twinEntity.getTwinClass().getExtendedClassIdSet().contains(twinStatusEntity.getTwinClassId());
    }


    @Transactional(rollbackFor = Throwable.class)
    public TwinStatusEntity createStatus(TwinClassEntity twinClassEntity, String key, String nameInDefaultLocale) throws ServiceException {
        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setKey(KeyUtils.lowerCaseNullSafe(key, ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT))
                .setNameI18nId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME, nameInDefaultLocale).getId());
        validateEntityAndThrow(twinStatusEntity, EntitySmartService.EntityValidateMode.beforeSave);
        TwinStatusEntity savedStatus = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        evictClassesCache(twinClassEntity);
        return savedStatus;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinStatusEntity createStatus(TwinStatusEntity twinStatusEntity, I18nEntity nameI18n, I18nEntity descriptionsI18n, FileData lightIcon, FileData darkIcon) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinStatusEntity.getTwinClassId());
        twinStatusEntity
                .setKey(KeyUtils.lowerCaseNullSafe(twinStatusEntity.getKey(), ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT))
                .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_STATUS_NAME, nameI18n).getId())
                .setDescriptionI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_STATUS_DESCRIPTION, descriptionsI18n).getId());
        validateEntityAndThrow(twinStatusEntity, EntitySmartService.EntityValidateMode.beforeSave);
        TwinStatusEntity savedStatus = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        evictClassesCache(twinClassEntity);
        return processIcons(savedStatus, lightIcon, darkIcon);
    }

    private void evictClassesCache(TwinClassEntity twinClassEntity) throws ServiceException {
        CacheUtils.evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, twinClassEntity.getId());
        CacheUtils.evictCache(cacheManager, TwinClassEntity.class.getSimpleName(), List.of(twinClassEntity.getId()));
        twinClassService.loadExtendsHierarchyChildClasses(twinClassEntity);
        if (KitUtils.isEmpty(twinClassEntity.getExtendsHierarchyChildClassKit()))
            return;
        CacheUtils.evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, twinClassEntity.getExtendsHierarchyChildClassKit().getIdSetSafe());
        CacheUtils.evictCache(cacheManager, TwinClassEntity.class.getSimpleName(), twinClassEntity.getExtendsHierarchyChildClassKit().getIdSetSafe());
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinStatusEntity updateStatus(TwinStatusEntity updateEntity, I18nEntity nameI18n, I18nEntity descriptionI18n, FileData lightIcon, FileData darkIcon) throws ServiceException {
        TwinStatusEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged(TwinStatusEntity.Fields.key, dbEntity.getKey(), updateEntity.getKey()))
            dbEntity.setKey(updateEntity.getKey());
        if (changesHelper.isChanged(TwinStatusEntity.Fields.backgroundColor, dbEntity.getBackgroundColor(), updateEntity.getBackgroundColor()))
            dbEntity.setBackgroundColor(updateEntity.getBackgroundColor());
        if (changesHelper.isChanged(TwinStatusEntity.Fields.fontColor, dbEntity.getFontColor(), updateEntity.getFontColor()))
            dbEntity.setFontColor(updateEntity.getFontColor());
        if (nameI18n != null) {
            if (dbEntity.getNameI18nId() != null)
                nameI18n.setId(dbEntity.getNameI18nId());
            i18nService.saveTranslations(I18nType.TWIN_STATUS_NAME, nameI18n);
            if (changesHelper.isChanged(TwinStatusEntity.Fields.nameI18nId, dbEntity.getNameI18nId(), nameI18n.getId()))
                dbEntity.setNameI18nId(nameI18n.getId()); // if new i18n was added
        }
        if (descriptionI18n != null) {
            if (dbEntity.getDescriptionI18nId() != null)
                descriptionI18n.setId(dbEntity.getDescriptionI18nId());
            i18nService.saveTranslations(I18nType.TWIN_STATUS_DESCRIPTION, descriptionI18n);
            if (changesHelper.isChanged(TwinStatusEntity.Fields.descriptionI18nId, dbEntity.getDescriptionI18nId(), descriptionI18n.getId()))
                dbEntity.setDescriptionI18nId(descriptionI18n.getId());  // if new i18n was added
        }
        updateTwinStatusIcons(dbEntity, lightIcon, darkIcon, changesHelper);
        dbEntity = updateSafe(dbEntity, changesHelper);
        if (changesHelper.hasChanges()) {
            evictClassesCache(dbEntity.getTwinClass());
        }
        return dbEntity;
    }

    public void updateTwinStatusIcons(TwinStatusEntity dbEntity, FileData iconLight, FileData iconDark, ChangesHelper changesHelper) throws ServiceException {
        if (iconLight != null) {
            ResourceEntity newValue = saveIconResourceIfExist(iconLight);
            if (changesHelper.isChanged(TwinStatusEntity.Fields.iconLightResourceId, dbEntity.getIconLightResourceId(), newValue.getId())) {
                dbEntity
                        .setIconLightResourceId(newValue.getId())
                        .setIconLightResource(newValue);
            }
        }
        if (iconDark != null) {
            ResourceEntity newValue = saveIconResourceIfExist(iconDark);
            if (changesHelper.isChanged(TwinStatusEntity.Fields.iconDarkResourceId, dbEntity.getIconDarkResourceId(), newValue.getId())) {
                dbEntity
                        .setIconLightResourceId(newValue.getId())
                        .setIconLightResource(newValue);
            }
        }
    }

    protected TwinStatusEntity processIcons(TwinStatusEntity twinStatusEntity, FileData lightIcon, FileData darkIcon) throws ServiceException {
        var lightIconEntity = saveIconResourceIfExist(lightIcon);
        var darkIconEntity = saveIconResourceIfExist(darkIcon);
        if (lightIconEntity != null) {
            twinStatusEntity.setIconLightResourceId(lightIconEntity.getId());
            twinStatusEntity.setIconLightResource(lightIconEntity);
        }
        if (darkIconEntity != null) {
            twinStatusEntity.setIconDarkResourceId(darkIconEntity.getId());
            twinStatusEntity.setIconDarkResource(darkIconEntity);
        }
        if (darkIconEntity != null || lightIconEntity != null) {
            twinStatusRepository.save(twinStatusEntity);
        }
        return twinStatusEntity;
    }


    private ResourceEntity saveIconResourceIfExist(FileData icon) throws ServiceException {
        if (icon != null) {
            return resourceService.addResource(icon.originalFileName(), icon.content());
        } else {
            return null;
        }
    }

    public boolean isSketch(UUID twinStatusId) {
        return SystemEntityService.TWIN_STATUS_SKETCH.equals(twinStatusId) || twinStatusRepository.existsByIdAndType(twinStatusId, StatusType.SKETCH);
    }
}
