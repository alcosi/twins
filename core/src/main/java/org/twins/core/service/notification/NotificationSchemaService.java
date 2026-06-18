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
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.notification.NotificationSchemaRepository;
import org.twins.core.domain.notification.NotificationSchemaCreate;
import org.twins.core.domain.notification.NotificationSchemaUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationSchemaService extends EntitySecureFindServiceImpl<NotificationSchemaEntity> {

    private final AuthService authService;
    private final NotificationSchemaRepository repository;
    private final I18nService i18nService;
    private final UserService userService;

    @Override
    public CrudRepository<NotificationSchemaEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<NotificationSchemaEntity, UUID> entityGetIdFunction() {
        return NotificationSchemaEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationSchemaEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(NotificationSchemaEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<NotificationSchemaEntity> createNotificationSchema(Collection<NotificationSchemaCreate> entities) throws ServiceException {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        i18nService.createI18nAndTranslations(I18nType.NOTIFICATION_SCHEMA_NAME,
                entities.stream().map(NotificationSchemaCreate::getNameI18n).filter(Objects::nonNull).toList());
        i18nService.createI18nAndTranslations(I18nType.NOTIFICATION_SCHEMA_DESCRIPTION,
                entities.stream().map(NotificationSchemaCreate::getDescriptionI18n).filter(Objects::nonNull).toList());

        List<NotificationSchemaEntity> entitiesToSave = new ArrayList<>();
        for (NotificationSchemaCreate notificationSchema : entities) {
            NotificationSchemaEntity entity = new NotificationSchemaEntity();
            entity
                    .setDomainId(authService.getApiUser().getDomainId())
                    .setNameI18nId(notificationSchema.getNameI18n() != null ? notificationSchema.getNameI18n().getId() : null)
                    .setDescriptionI18nId(notificationSchema.getDescriptionI18n() != null ? notificationSchema.getDescriptionI18n().getId() : null)
                    .setCreatedByUserId(authService.getApiUser().getUserId())
                    .setCreatedAt(Timestamp.from(Instant.now()));
            entitiesToSave.add(entity);
        }

        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<NotificationSchemaEntity> updateNotificationSchema(Collection<NotificationSchemaUpdate> entities) throws ServiceException {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<NotificationSchemaEntity> changes = new ChangesHelperMulti<>();
        Kit<NotificationSchemaEntity, UUID> entitiesKit = findEntitiesSafe(entities.stream().map(NotificationSchemaUpdate::getId).toList());
        List<NotificationSchemaEntity> allEntities = new ArrayList<>(entities.size());

        for (NotificationSchemaUpdate notificationSchema : entities) {
            NotificationSchemaEntity entity = entitiesKit.get(notificationSchema.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(notificationSchema.getNameI18n(), I18nType.NOTIFICATION_SCHEMA_NAME, entity,
                    NotificationSchemaEntity::getNameI18nId, NotificationSchemaEntity::setNameI18nId,
                    NotificationSchemaEntity.Fields.nameI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(notificationSchema.getDescriptionI18n(), I18nType.NOTIFICATION_SCHEMA_DESCRIPTION, entity,
                    NotificationSchemaEntity::getDescriptionI18nId, NotificationSchemaEntity::setDescriptionI18nId,
                    NotificationSchemaEntity.Fields.descriptionI18nId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);
        return allEntities;
    }

    public void loadCreatedByUser(NotificationSchemaEntity entity) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(entity));
    }

    public void loadCreatedByUser(Collection<NotificationSchemaEntity> entities) throws ServiceException {
        userService.load(entities,
                NotificationSchemaEntity::getCreatedByUserId,
                NotificationSchemaEntity::getCreatedByUser,
                NotificationSchemaEntity::setCreatedByUser);
    }
}
