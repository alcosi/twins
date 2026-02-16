package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapRepository;
import org.twins.core.domain.notification.HistoryNotificationSchemaMapCreate;
import org.twins.core.domain.notification.HistoryNotificationSchemaMapUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class HistoryNotificationSchemaMapService extends EntitySecureFindServiceImpl<HistoryNotificationSchemaMapEntity> {

    private final HistoryNotificationSchemaMapRepository repository;

    @Override
    public CrudRepository<HistoryNotificationSchemaMapEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationSchemaMapEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationSchemaMapEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationSchemaMapEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationSchemaMapEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationSchemaMapEntity> createHistoryNotificationSchemaMap(List<HistoryNotificationSchemaMapCreate> schemaMaps) throws ServiceException {
        if (schemaMaps == null || schemaMaps.isEmpty()) {
            return Collections.emptyList();
        }

        List<HistoryNotificationSchemaMapEntity> entitiesToSave = new ArrayList<>();

        for (HistoryNotificationSchemaMapCreate schemaMap : schemaMaps) {
            HistoryNotificationSchemaMapEntity sourceEntity = schemaMap.getHistoryNotificationSchemaMap();
            HistoryNotificationSchemaMapEntity entity = new HistoryNotificationSchemaMapEntity()
                    .setHistoryTypeId(sourceEntity.getHistoryTypeId())
                    .setTwinClassId(sourceEntity.getTwinClassId())
                    .setTwinClassFieldId(sourceEntity.getTwinClassFieldId())
                    .setTwinValidatorSetId(sourceEntity.getTwinValidatorSetId())
                    .setTwinValidatorSetInvert(sourceEntity.getTwinValidatorSetInvert())
                    .setNotificationSchemaId(sourceEntity.getNotificationSchemaId())
                    .setHistoryNotificationRecipientId(sourceEntity.getHistoryNotificationRecipientId())
                    .setNotificationChannelEventId(sourceEntity.getNotificationChannelEventId());

            entitiesToSave.add(entity);
        }

        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationSchemaMapEntity> updateHistoryNotificationSchemaMap(List<HistoryNotificationSchemaMapUpdate> schemaMaps) throws ServiceException {
        if (schemaMaps == null || schemaMaps.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<HistoryNotificationSchemaMapEntity> changes = new ChangesHelperMulti<>();
        Kit<HistoryNotificationSchemaMapEntity, UUID> entitiesKit = findEntitiesSafe(schemaMaps.stream().map(HistoryNotificationSchemaMapUpdate::getId).toList());
        List<HistoryNotificationSchemaMapEntity> allEntities = new ArrayList<>(schemaMaps.size());

        for (HistoryNotificationSchemaMapUpdate schemaMap : schemaMaps) {
            HistoryNotificationSchemaMapEntity entity = entitiesKit.get(schemaMap.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            HistoryNotificationSchemaMapEntity sourceEntity = schemaMap.getHistoryNotificationSchemaMap();
            updateEntityFieldByValue(sourceEntity.getHistoryTypeId(), entity, HistoryNotificationSchemaMapEntity::getHistoryTypeId, HistoryNotificationSchemaMapEntity::setHistoryTypeId, HistoryNotificationSchemaMapEntity.Fields.historyTypeId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinClassId(), entity, HistoryNotificationSchemaMapEntity::getTwinClassId, HistoryNotificationSchemaMapEntity::setTwinClassId, HistoryNotificationSchemaMapEntity.Fields.twinClassId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinClassFieldId(), entity, HistoryNotificationSchemaMapEntity::getTwinClassFieldId, HistoryNotificationSchemaMapEntity::setTwinClassFieldId, HistoryNotificationSchemaMapEntity.Fields.twinClassFieldId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinValidatorSetId(), entity, HistoryNotificationSchemaMapEntity::getTwinValidatorSetId, HistoryNotificationSchemaMapEntity::setTwinValidatorSetId, HistoryNotificationSchemaMapEntity.Fields.twinValidatorSetId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinValidatorSetInvert(), entity, HistoryNotificationSchemaMapEntity::getTwinValidatorSetInvert, HistoryNotificationSchemaMapEntity::setTwinValidatorSetInvert, HistoryNotificationSchemaMapEntity.Fields.twinValidatorSetInvert, changesHelper);
            updateEntityFieldByValue(sourceEntity.getNotificationSchemaId(), entity, HistoryNotificationSchemaMapEntity::getNotificationSchemaId, HistoryNotificationSchemaMapEntity::setNotificationSchemaId, HistoryNotificationSchemaMapEntity.Fields.notificationSchemaId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getHistoryNotificationRecipientId(), entity, HistoryNotificationSchemaMapEntity::getHistoryNotificationRecipientId, HistoryNotificationSchemaMapEntity::setHistoryNotificationRecipientId, HistoryNotificationSchemaMapEntity.Fields.historyNotificationRecipientId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getNotificationChannelEventId(), entity, HistoryNotificationSchemaMapEntity::getNotificationChannelEventId, HistoryNotificationSchemaMapEntity::setNotificationChannelEventId, HistoryNotificationSchemaMapEntity.Fields.notificationChannelEventId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}
