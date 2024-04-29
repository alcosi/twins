package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatusService extends EntitySecureFindServiceImpl<TwinStatusEntity> {
    final TwinStatusRepository twinStatusRepository;
    final TwinClassService twinClassService;
    final I18nService i18nService;

    @Override
    public CrudRepository<TwinStatusEntity, UUID> entityRepository() {
        return twinStatusRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatusEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatusEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
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

    @Transactional
    public TwinStatusEntity createStatus(TwinClassEntity twinClassEntity, String key, String nameInDefaultLocale) throws ServiceException {
        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setKey(key);
        return createStatus(twinStatusEntity, nameInDefaultLocale, "");
    }

    @Transactional
    public TwinStatusEntity createStatus(TwinStatusEntity twinStatusEntity, String nameInDefaultLocale, String descriptionInDefaultLocale) throws ServiceException {
        twinStatusEntity
                .setNameI18nId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME, nameInDefaultLocale).getI18nId())
                .setDescriptionI18nId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME, descriptionInDefaultLocale).getI18nId());
        return entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }
}
