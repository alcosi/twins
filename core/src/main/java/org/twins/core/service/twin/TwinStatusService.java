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
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.twinstatus.TwinStatusDuplicate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.status.StatusType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;


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

    public void loadStatusesForTwinClasses(TwinClassEntity twinClassEntity) {
        loadStatusesForTwinClasses(Collections.singletonList(twinClassEntity));
    }

    public void loadStatusesForTwinClasses(Collection<TwinClassEntity> twinClassEntities) {
        Kit<TwinClassEntity, UUID> needLoad = new Kit<>(TwinClassEntity::getId);
        Set<UUID> extendsClassesSet = new HashSet<>();
        for (TwinClassEntity twinClassEntity : twinClassEntities) {
            if (twinClassEntity.getTwinStatusKit() != null)
                continue;
            needLoad.add(twinClassEntity);
            twinClassEntity.setTwinStatusKit(new Kit<>(TwinStatusEntity::getId));
            if (twinClassEntity.getExtendedClassIdSet().size() > 1)
                extendsClassesSet.addAll(twinClassEntity.getExtendedClassIdSet().stream().filter(t -> !twinClassEntity.getId().equals(t)).toList());
        }
        if (needLoad.isEmpty())
            return;

        List<TwinStatusEntity> twinStatusEntityList = twinStatusRepository.findByTwinClassIdIn(needLoad.getIdSet(), extendsClassesSet);
        if (CollectionUtils.isEmpty(twinStatusEntityList))
            return;
        Map<UUID, List<TwinStatusEntity>> statussMap = new HashMap<>(); // key - twinClassId
        for (TwinStatusEntity twinStatusEntity : twinStatusEntityList) { // grouping by twinClassId
            statussMap.computeIfAbsent(twinStatusEntity.getTwinClassId(), k -> new ArrayList<>());
            statussMap.get(twinStatusEntity.getTwinClassId()).add(twinStatusEntity);
        }
        for (var twinClassEntity : needLoad) {
            for (UUID extendsTwinClassId : twinClassEntity.getExtendedClassIdSet()) {
                var statuses = statussMap.get(extendsTwinClassId);
                if (statuses == null)
                    continue;
                if (extendsTwinClassId.equals(twinClassEntity.getId())) {
                    twinClassEntity.getTwinStatusKit().addAll(statuses); // we can add all statuses
                    continue;
                }
                for (var statusInherited : statuses) {
                    if (Boolean.TRUE.equals(statusInherited.getInheritable()))
                        twinClassEntity.getTwinStatusKit().add(statusInherited);
                }
            }
        }
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
        i18nService.updateI18nFieldForEntity(nameI18n, I18nType.TWIN_STATUS_NAME, dbEntity, TwinStatusEntity::getNameI18nId, TwinStatusEntity::setNameI18nId, TwinStatusEntity.Fields.nameI18nId, changesHelper);
        i18nService.updateI18nFieldForEntity(descriptionI18n, I18nType.TWIN_STATUS_DESCRIPTION, dbEntity, TwinStatusEntity::getDescriptionI18nId, TwinStatusEntity::setDescriptionI18nId, TwinStatusEntity.Fields.descriptionI18nId, changesHelper);
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

    public void duplicateStatusesForClass(TwinClassEntity fromTwinClass, TwinClassEntity toTwinClass) throws ServiceException {
        loadStatusesForTwinClasses(fromTwinClass);
        if (KitUtils.isEmpty(fromTwinClass.getTwinStatusKit())) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinStatusEntity>();
        TwinStatusEntity duplicateStatusEntity;
        for (var originalStatus : fromTwinClass.getTwinStatusKit().getCollection()) {
            if (!originalStatus.getTwinClassId().equals(fromTwinClass.getId()))
                continue; //skipping inherited statuses
            duplicateStatusEntity = duplicateStatusEntity(originalStatus, toTwinClass, originalStatus.getKey()); // we can copy the status with the same key
            setI18nForDuplicate(originalStatus, duplicateStatusEntity);
            entitiesForSave.add(duplicateStatusEntity);
        }
        //todo check uniq id and key before safe
        saveSafe(entitiesForSave);
    }

    @Transactional
    public Collection<TwinStatusEntity> duplicate(Collection<TwinStatusDuplicate> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        var newKeys = new HashSet<String>();
        for (var duplicate : duplicates) {
            if (newKeys.contains(duplicate.getNewKey()))
                throw new ServiceException(ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT, "twinStatus key[" + duplicate.getNewKey() + "] is duplicated in request");
            else
                newKeys.add(duplicate.getNewKey());
        }
        loadOriginalTwinStatus(duplicates);
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinClassId() == null)
                duplicate
                        .setNewTwinClassId(duplicate.getOriginalTwinStatus().getTwinClassId()) // same class
                        .setNewTwinClass(duplicate.getOriginalTwinStatus().getTwinClass());
        }
        loadNewClasses(duplicates);
        var entitiesForSave = new ArrayList<TwinStatusEntity>();
        TwinStatusEntity duplicateStatusEntity;
        for (var duplicate : duplicates) {

            duplicateStatusEntity = duplicateStatusEntity(duplicate.getOriginalTwinStatus(), duplicate.getNewTwinClass(), duplicate.getNewKey());
            setI18nForDuplicate(duplicate.getOriginalTwinStatus(), duplicateStatusEntity);
            entitiesForSave.add(duplicateStatusEntity);
            if (duplicate.isDuplicateTriggers()) {
                //todo implement in future
            }
        }
        //todo check uniq id and key before safe
        return StreamSupport.stream(saveSafe(entitiesForSave).spliterator(), false).toList();
    }

    private TwinStatusEntity duplicateStatusEntity(TwinStatusEntity srcFieldEntity, TwinClassEntity duplicateTwinClass, String newKey) throws ServiceException {
        log.info("{} will be duplicated for {}", srcFieldEntity.logNormal(), duplicateTwinClass.logNormal());

        return new TwinStatusEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(newKey, ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT))
                .setTwinClassId(duplicateTwinClass.getId())
                .setTwinClass(duplicateTwinClass)
                .setInheritable(srcFieldEntity.getInheritable())
                .setBackgroundColor(srcFieldEntity.getBackgroundColor())
                .setFontColor(srcFieldEntity.getFontColor())
                .setType(srcFieldEntity.getType());
    }

    private void setI18nForDuplicate(TwinStatusEntity src, TwinStatusEntity dst) {
        //todo change to bulk
        if (src.getNameI18nId() != null) {
            dst.setNameI18nId(i18nService.duplicateI18n(src.getNameI18nId()).getId());
        }
        if (src.getDescriptionI18nId() != null) {
            dst.setDescriptionI18nId(i18nService.duplicateI18n(src.getDescriptionI18nId()).getId());
        }
    }

    private void loadOriginalTwinStatus(Collection<TwinStatusDuplicate> duplicates) throws ServiceException {
        load(duplicates,
                TwinStatusDuplicate::getNewTwinStatusId,
                TwinStatusDuplicate::getOriginalTwinStatusId,
                TwinStatusDuplicate::getOriginalTwinStatus,
                TwinStatusDuplicate::setOriginalTwinStatus);
    }

    private void loadNewClasses(Collection<TwinStatusDuplicate> duplicates) throws ServiceException {
        twinClassService.load(duplicates,
                TwinStatusDuplicate::getNewTwinStatusId,
                TwinStatusDuplicate::getNewTwinClassId,
                TwinStatusDuplicate::getNewTwinClass,
                TwinStatusDuplicate::setNewTwinClass);
    }
}
