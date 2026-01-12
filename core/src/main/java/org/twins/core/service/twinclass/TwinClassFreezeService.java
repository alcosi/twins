package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.dao.twinclass.TwinClassFreezeRepository;
import org.twins.core.domain.twinclass.TwinClassFreezeCreate;
import org.twins.core.domain.twinclass.TwinClassFreezeUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinStatusService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFreezeService extends EntitySecureFindServiceImpl<TwinClassFreezeEntity> {
    private final TwinClassFreezeRepository repository;
    private final TwinStatusService twinStatusService;
    private final I18nService i18nService;

    @Override
    public CrudRepository<TwinClassFreezeEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFreezeEntity, UUID> entityGetIdFunction() {
        return TwinClassFreezeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFreezeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFreezeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getKey() == null) {
            return logErrorAndReturnFalse(entity.logNormal() + " empty key");
        }
        if (entity.getTwinStatusId() == null) {
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinStatusId");
        }

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinStatus() == null || !entity.getTwinStatus().getId().equals(entity.getTwinStatusId())) {
                    entity.setTwinStatus(twinStatusService.findEntitySafe(entity.getTwinStatusId()));
                }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFreezeEntity> createTwinClassFreezeList(List<TwinClassFreezeCreate> freezeCreates) throws ServiceException {
        if (CollectionUtils.isEmpty(freezeCreates)) {
            return Collections.emptyList();
        }

        i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FREEZE_NAME, freezeCreates.stream().map(TwinClassFreezeCreate::getName).toList());
        i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FREEZE_DESCRIPTION, freezeCreates.stream().map(TwinClassFreezeCreate::getDescription).toList());

        List<TwinClassFreezeEntity> twinClassFreezeEntities = new ArrayList<>();

        for (TwinClassFreezeCreate freezeCreate : freezeCreates) {
            TwinClassFreezeEntity twinClassFreezeEntity = new TwinClassFreezeEntity();
            twinClassFreezeEntity
                    .setKey(freezeCreate.getKey())
                    .setTwinStatusId(freezeCreate.getStatusId())
                    .setNameI18NId(freezeCreate.getName() != null ? freezeCreate.getName().getId() : null)
                    .setDescriptionI18NId(freezeCreate.getDescription() != null ? freezeCreate.getDescription().getId() : null);
            twinClassFreezeEntities.add(twinClassFreezeEntity);
        }
        validateEntitiesAndThrow(twinClassFreezeEntities, EntitySmartService.EntityValidateMode.beforeSave);

        return StreamSupport.stream(entityRepository().saveAll(twinClassFreezeEntities).spliterator(), false).toList();
    }


    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFreezeEntity> updateTwinClassFreezeList(List<TwinClassFreezeUpdate> freezeUpdates) throws ServiceException {
        if (CollectionUtils.isEmpty(freezeUpdates)) {
            return Collections.emptyList();
        }

        Kit<TwinClassFreezeEntity, UUID> dbFreezeEntitiesKit = findEntitiesSafe(
                freezeUpdates.stream()
                        .map(TwinClassFreezeUpdate::getId)
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinClassFreezeEntity> changes = new ChangesHelperMulti<>();
        List<TwinClassFreezeEntity> allEntities = dbFreezeEntitiesKit.getList();

        for (TwinClassFreezeUpdate freezeUpdate : freezeUpdates) {
            TwinClassFreezeEntity dbTwinClassFreezeEntity = dbFreezeEntitiesKit.get(freezeUpdate.getId());
            ChangesHelper changesHelper = new ChangesHelper();

            i18nService.updateI18nFieldForEntity(freezeUpdate.getName(), I18nType.TWIN_CLASS_FREEZE_NAME, dbTwinClassFreezeEntity, TwinClassFreezeEntity::getNameI18NId, TwinClassFreezeEntity::setNameI18NId, TwinClassFreezeEntity.Fields.nameI18NId, changesHelper);
            i18nService.updateI18nFieldForEntity(freezeUpdate.getDescription(), I18nType.TWIN_CLASS_FREEZE_DESCRIPTION, dbTwinClassFreezeEntity, TwinClassFreezeEntity::getDescriptionI18NId, TwinClassFreezeEntity::setDescriptionI18NId, TwinClassFreezeEntity.Fields.descriptionI18NId, changesHelper);
            updateEntityFieldByValue(freezeUpdate.getKey(), dbTwinClassFreezeEntity, TwinClassFreezeEntity::getKey, TwinClassFreezeEntity::setKey, TwinClassFreezeEntity.Fields.key, changesHelper);
            updateEntityFieldByValue(freezeUpdate.getStatusId(), dbTwinClassFreezeEntity, TwinClassFreezeEntity::getTwinStatusId, TwinClassFreezeEntity::setTwinStatusId, TwinClassFreezeEntity.Fields.twinStatusId, changesHelper);

            changes.add(dbTwinClassFreezeEntity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

}
