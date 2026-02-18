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
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationRepository;
import org.twins.core.domain.notification.HistoryNotificationCreate;
import org.twins.core.domain.notification.HistoryNotificationUpdate;
import org.twins.core.service.auth.AuthService;

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
public class HistoryNotificationService extends EntitySecureFindServiceImpl<HistoryNotificationEntity> {

    private final HistoryNotificationRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<HistoryNotificationEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationEntity> createHistoryNotification(List<HistoryNotificationCreate> notifications) throws ServiceException {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }

        List<HistoryNotificationEntity> entitiesToSave = new ArrayList<>();

        for (HistoryNotificationCreate notification : notifications) {
            HistoryNotificationEntity entity = notification.getHistoryNotification();
            entity.setCreatedByUserId(authService.getApiUser().getUserId());

            entitiesToSave.add(entity);
        }

        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationEntity> updateHistoryNotification(List<HistoryNotificationUpdate> notifications) throws ServiceException {
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<HistoryNotificationEntity> changes = new ChangesHelperMulti<>();
        Kit<HistoryNotificationEntity, UUID> entitiesKit = findEntitiesSafe(notifications.stream().map(HistoryNotificationUpdate::getId).toList());
        List<HistoryNotificationEntity> allEntities = new ArrayList<>(notifications.size());

        for (HistoryNotificationUpdate notification : notifications) {
            HistoryNotificationEntity entity = entitiesKit.get(notification.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            HistoryNotificationEntity sourceEntity = notification.getHistoryNotification();
            updateEntityFieldByValue(sourceEntity.getHistoryTypeId(), entity, HistoryNotificationEntity::getHistoryTypeId, HistoryNotificationEntity::setHistoryTypeId, HistoryNotificationEntity.Fields.historyTypeId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinClassId(), entity, HistoryNotificationEntity::getTwinClassId, HistoryNotificationEntity::setTwinClassId, HistoryNotificationEntity.Fields.twinClassId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinClassFieldId(), entity, HistoryNotificationEntity::getTwinClassFieldId, HistoryNotificationEntity::setTwinClassFieldId, HistoryNotificationEntity.Fields.twinClassFieldId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinValidatorSetId(), entity, HistoryNotificationEntity::getTwinValidatorSetId, HistoryNotificationEntity::setTwinValidatorSetId, HistoryNotificationEntity.Fields.twinValidatorSetId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getTwinValidatorSetInvert(), entity, HistoryNotificationEntity::getTwinValidatorSetInvert, HistoryNotificationEntity::setTwinValidatorSetInvert, HistoryNotificationEntity.Fields.twinValidatorSetInvert, changesHelper);
            updateEntityFieldByValue(sourceEntity.getNotificationSchemaId(), entity, HistoryNotificationEntity::getNotificationSchemaId, HistoryNotificationEntity::setNotificationSchemaId, HistoryNotificationEntity.Fields.notificationSchemaId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getHistoryNotificationRecipientId(), entity, HistoryNotificationEntity::getHistoryNotificationRecipientId, HistoryNotificationEntity::setHistoryNotificationRecipientId, HistoryNotificationEntity.Fields.historyNotificationRecipientId, changesHelper);
            updateEntityFieldByValue(sourceEntity.getNotificationChannelEventId(), entity, HistoryNotificationEntity::getNotificationChannelEventId, HistoryNotificationEntity::setNotificationChannelEventId, HistoryNotificationEntity.Fields.notificationChannelEventId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}
