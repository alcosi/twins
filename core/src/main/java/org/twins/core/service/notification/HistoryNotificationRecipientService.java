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
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientRepository;
import org.twins.core.domain.notification.HistoryNotificationRecipientCreate;
import org.twins.core.domain.notification.HistoryNotificationRecipientUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;

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

        i18nService.createI18nAndTranslations(I18nType.RECIPIENT_NAME,
                recipients
                        .stream().map(HistoryNotificationRecipientCreate::getNameI18n)
                        .toList());

        //todo save description

        List<HistoryNotificationRecipientEntity> recipientsToSave = new ArrayList<>();

        for (HistoryNotificationRecipientCreate recipient : recipients) {
            HistoryNotificationRecipientEntity recipientEntity = new HistoryNotificationRecipientEntity()
                    .setNameI18nId(recipient.getHistoryNotificationRecipient().getNameI18n().getId())
                    .setDescriptionI18nId(recipient.getHistoryNotificationRecipient().getDescriptionI18n().getId())
                    .setCreatedAt(recipient.getHistoryNotificationRecipient().getCreatedAt())
                    .setCreatedByUserId(recipient.getHistoryNotificationRecipient().getCreatedByUserId())
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
            updateHistoryNotificationRecipientName(recipient.getNameI18n(), entity, changesHelper);
            updateHistoryNotificationRecipientDescription(recipient.getDescriptionI18n(), entity, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    private void updateHistoryNotificationRecipientName(I18nEntity nameI18n, HistoryNotificationRecipientEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbEntity.getNameI18nId() != null)
            nameI18n.setId(dbEntity.getNameI18nId());
        i18nService.saveTranslations(I18nType.RECIPIENT_NAME, nameI18n);
        //todo changesHelper for i18n doesn't work
        if (changesHelper.isChanged(HistoryNotificationRecipientEntity.Fields.nameI18nId, dbEntity.getNameI18nId(), nameI18n.getId()))
            dbEntity.setNameI18nId(nameI18n.getId());
    }

    private void updateHistoryNotificationRecipientDescription(I18nEntity descriptionI18n, HistoryNotificationRecipientEntity dbEntity, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbEntity.getDescriptionI18nId() != null)
            descriptionI18n.setId(dbEntity.getDescriptionI18nId());
        i18nService.saveTranslations(I18nType.RECIPIENT_DESCRIPTION, descriptionI18n);
        //todo changesHelper for i18n doesn't work
        if (changesHelper.isChanged(DataListOptionEntity.Fields.descriptionI18nId, dbEntity.getDescriptionI18nId(), descriptionI18n.getId()))
            dbEntity.setDescriptionI18nId(descriptionI18n.getId());
    }
}
