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
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientRepository;
import org.twins.core.domain.notification.HistoryNotificationRecipientCreate;
import org.twins.core.domain.notification.HistoryNotificationRecipientUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryNotificationRecipientService extends EntitySecureFindServiceImpl<HistoryNotificationRecipientEntity> {
    private final HistoryNotificationRecipientRepository repository;
    private final I18nService i18nService;
    private final AuthService authService;

    @Override
    public CrudRepository<HistoryNotificationRecipientEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationRecipientEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationRecipientEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationRecipientEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationRecipientEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationRecipientEntity> createHistoryNotificationRecipients(List<HistoryNotificationRecipientCreate> recipients) throws ServiceException {
        if (recipients == null || recipients.isEmpty()) {
            return Collections.emptyList();
        }

        i18nService.createI18nAndTranslations(I18nType.HISTORY_NOTIFICATION_RECIPIENT_NAME,
                recipients
                        .stream().map(HistoryNotificationRecipientCreate::getNameI18n)
                        .toList());

        //todo save description

        List<HistoryNotificationRecipientEntity> recipientsToSave = new ArrayList<>();

        for (HistoryNotificationRecipientCreate recipient : recipients) {
            HistoryNotificationRecipientEntity recipientEntity = new HistoryNotificationRecipientEntity()
                    .setNameI18nId(recipient.getHistoryNotificationRecipient().getNameI18n().getId())
                    .setDescriptionI18nId(recipient.getHistoryNotificationRecipient().getDescriptionI18n().getId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(authService.getApiUser().getUserId())
                    .setDomainId(recipient.getHistoryNotificationRecipient().getNameI18n().getDomainId());

            recipientsToSave.add(recipientEntity);
        }

        return StreamSupport.stream(saveSafe(recipientsToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationRecipientEntity> updateHistoryNotificationRecipients(List<HistoryNotificationRecipientUpdate> recipients) throws ServiceException {
        if (recipients == null || recipients.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<HistoryNotificationRecipientEntity> changes = new ChangesHelperMulti<>();
        List<HistoryNotificationRecipientEntity> allEntities = new ArrayList<>(recipients.size());

        Kit<HistoryNotificationRecipientEntity, UUID> entitiesKit = findEntitiesSafe(recipients.stream().map(HistoryNotificationRecipientUpdate::getId).toList());

        for (HistoryNotificationRecipientUpdate recipient : recipients) {
            HistoryNotificationRecipientEntity entity = entitiesKit.get(recipient.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(recipient.getNameI18n(), I18nType.HISTORY_NOTIFICATION_RECIPIENT_NAME, entity,
                    HistoryNotificationRecipientEntity::getNameI18nId, HistoryNotificationRecipientEntity::setNameI18nId,
                    HistoryNotificationRecipientEntity.Fields.nameI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(recipient.getDescriptionI18n(), I18nType.HISTORY_NOTIFICATION_RECIPIENT_DESCRIPTION, entity,
                    HistoryNotificationRecipientEntity::getDescriptionI18nId, HistoryNotificationRecipientEntity::setDescriptionI18nId,
                    HistoryNotificationRecipientEntity.Fields.descriptionI18nId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}
