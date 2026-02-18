package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
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
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationRepository;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.notification.HistoryNotificationCreate;
import org.twins.core.domain.notification.HistoryNotificationUpdate;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.sql.Timestamp;
import java.time.Instant;
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
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinValidatorSetService twinValidatorSetService;
    private final NotificationSchemaService notificationSchemaService;
    private final NotificationEventServiceService notificationEventServiceService;

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
        ApiUser apiUser = authService.getApiUser();
        if (entity.getHistoryNotificationRecipient().getDomainId() != null
                && !entity.getHistoryNotificationRecipient().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logNormal() + " is not allowed in " + apiUser.getDomain().logNormal());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entityValidateMode != EntitySmartService.EntityValidateMode.beforeSave) {
            return true;
        }
        ApiUser apiUser = authService.getApiUser();
        UUID currentDomainId = apiUser.getDomain().getId();

        // Check twinClassId
        if (entity.getTwinClassId() != null) {
            TwinClassEntity twinClass = entity.getTwinClass();
            if (twinClass == null || !twinClass.getId().equals(entity.getTwinClassId())) {
                twinClass = twinClassService.findEntitySafe(entity.getTwinClassId());
            }
            if (twinClass.getDomainId() != null && !twinClass.getDomainId().equals(currentDomainId)) {
                return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " twinClass[" + twinClass.easyLog(EasyLoggable.Level.SHORT) + "] is from different domain");
            }
        }

        // Check twinClassFieldId (through twinClass)
        if (entity.getTwinClassFieldId() != null) {
            TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(entity.getTwinClassFieldId());

            if (twinClassField.getTwinClass() != null && twinClassField.getTwinClass().getDomainId() != null
                    && !twinClassField.getTwinClass().getDomainId().equals(currentDomainId)) {
                return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " twinClassField[" + twinClassField.easyLog(EasyLoggable.Level.SHORT) + "] is from different domain");
            }
        }

        // Check twinValidatorSetId
        if (entity.getTwinValidatorSetId() != null) {
            TwinValidatorSetEntity twinValidatorSet = entity.getTwinValidatorSet();
            if (twinValidatorSet == null || !twinValidatorSet.getId().equals(entity.getTwinValidatorSetId())) {
                twinValidatorSet = twinValidatorSetService.findEntitySafe(entity.getTwinValidatorSetId());
            }
            if (twinValidatorSet.getDomainId() != null && !twinValidatorSet.getDomainId().equals(currentDomainId)) {
                return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " twinValidatorSet[" + twinValidatorSet.getId() + "] is from different domain");
            }
        }

        // Check notificationSchemaId
        if (entity.getNotificationSchemaId() != null) {
            NotificationSchemaEntity notificationSchema = entity.getNotificationSchema();
            if (notificationSchema == null || !notificationSchema.getId().equals(entity.getNotificationSchemaId())) {
                notificationSchema = (notificationSchemaService.findEntitySafe(entity.getNotificationSchemaId()));
            }
            if (notificationSchema.getDomainId() != null && !notificationSchema.getDomainId().equals(currentDomainId)) {
                return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " notificationSchema[" + notificationSchema.getId() + "] is from different domain");
            }
        }

        // Check notificationChannelEventId (through notificationChannel)
        if (entity.getNotificationChannelEventId() != null) {
            NotificationChannelEventEntity notificationChannelEvent = entity.getNotificationChannelEvent();
            if (notificationChannelEvent == null || !notificationChannelEvent.getId().equals(entity.getNotificationChannelEventId())) {
                notificationChannelEvent = notificationEventServiceService.findEntitySafe(entity.getNotificationChannelEventId());
            }
            if (notificationChannelEvent.getNotificationChannel() != null
                    && notificationChannelEvent.getNotificationChannel().getDomainId() != null
                    && !notificationChannelEvent.getNotificationChannel().getDomainId().equals(currentDomainId)) {
                return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " notificationChannelEvent[" + entity.getNotificationChannelEvent().getId() + "] is from different domain");
            }
        }

        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationEntity> createHistoryNotification(List<HistoryNotificationCreate> notifications) throws ServiceException {
        if (CollectionUtils.isEmpty(notifications)) {
            return Collections.emptyList();
        }

        List<HistoryNotificationEntity> entitiesToSave = new ArrayList<>();

        for (HistoryNotificationCreate notification : notifications) {
            HistoryNotificationEntity entity = notification.getHistoryNotification();
            entity.setCreatedByUserId(authService.getApiUser().getUserId());
            entity.setCreatedAt(Timestamp.from(Instant.now()));

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
