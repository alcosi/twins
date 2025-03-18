package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;

import static org.cambium.common.util.CacheUtils.evictCache;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatusService extends EntitySecureFindServiceImpl<TwinStatusEntity> {
    final TwinStatusRepository twinStatusRepository;
    final TwinClassService twinClassService;
    final I18nService i18nService;

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
                //todo validate that status is uniq in class
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafeCached(entity.getTwinClassId()));
        }
        return true;
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
    public TwinStatusEntity createStatus(TwinStatusEntity twinStatusEntity, I18nEntity nameI18n, I18nEntity descriptionsI18n) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntitySafe(twinStatusEntity.getTwinClassId());
        twinStatusEntity
                .setKey(KeyUtils.lowerCaseNullSafe(twinStatusEntity.getKey(), ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT))
                .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_STATUS_NAME, nameI18n).getId())
                .setDescriptionI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_STATUS_DESCRIPTION, descriptionsI18n).getId());
        validateEntityAndThrow(twinStatusEntity, EntitySmartService.EntityValidateMode.beforeSave);
        TwinStatusEntity savedStatus = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        evictClassesCache(twinClassEntity);
        return savedStatus;
    }

    private void evictClassesCache(TwinClassEntity twinClassEntity) throws ServiceException {
        evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, twinClassEntity.getId());
        twinClassService.loadExtendsHierarchyChildClasses(twinClassEntity);
        if (KitUtils.isEmpty(twinClassEntity.getExtendsHierarchyChildClassKit()))
            return;
        for (var childClass : twinClassEntity.getExtendsHierarchyChildClassKit()) {
            evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, childClass.getId());
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinStatusEntity updateStatus(TwinStatusEntity updateEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinStatusEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged(TwinStatusEntity.Fields.key, dbEntity.getKey(), updateEntity.getKey()))
            dbEntity.setKey(updateEntity.getKey());
        if (changesHelper.isChanged(TwinStatusEntity.Fields.backgroundColor, dbEntity.getBackgroundColor(), updateEntity.getBackgroundColor()))
            dbEntity.setBackgroundColor(updateEntity.getBackgroundColor());
        if (changesHelper.isChanged(TwinStatusEntity.Fields.fontColor, dbEntity.getFontColor(), updateEntity.getFontColor()))
            dbEntity.setFontColor(updateEntity.getFontColor());
        if (changesHelper.isChanged(TwinStatusEntity.Fields.logo, dbEntity.getLogo(), updateEntity.getLogo()))
            dbEntity.setLogo(updateEntity.getLogo());
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
        dbEntity = updateSafe(dbEntity, changesHelper);
        if (changesHelper.hasChanges()) {
            evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, dbEntity.getTwinClassId()); //todo also inherited
        }
        return dbEntity;
    }

    private void evictCache(TwinStatusEntity status) throws ServiceException {
        Map<String, List<Object>> cacheEntries = Map.of(
                TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, List.of(status.getTwinClassId()),
                TwinClassEntity.class.getSimpleName(), List.of(status.getTwinClassId())
        );
        CacheUtils.evictCache(cacheManager, cacheEntries);
    }


}
