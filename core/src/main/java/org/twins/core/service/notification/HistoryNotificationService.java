package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
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
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationRepository;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.notification.HistoryNotificationCreate;
import org.twins.core.domain.notification.HistoryNotificationUpdate;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.core.service.twinvalidator.TwinValidatorSetService;
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
public class HistoryNotificationService extends EntitySecureFindServiceImpl<HistoryNotificationEntity> {

    private final HistoryNotificationRepository repository;
    private final AuthService authService;
    private final UserService userService;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinValidatorSetService twinValidatorSetService;
    private final NotificationSchemaService notificationSchemaService;
    private final NotificationEventServiceService notificationEventServiceService;
    private final HistoryNotificationRecipientService historyNotificationRecipientService;

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
        loadHistoryNotificationRecipient(entity);
        if (entity.getHistoryNotificationRecipient().getDomainId() != null
                && !entity.getHistoryNotificationRecipient().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logNormal() + " is not allowed in " + apiUser.getDomain().logNormal());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        }
        if (entity.getNotificationSchemaId() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty notificationSchemaId");
        }
        if (entity.getHistoryNotificationRecipientId() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty historyNotificationRecipientId");
        }
        if (entity.getNotificationChannelEventId() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty notificationChannelEventId");
        }
        if (entityValidateMode != EntitySmartService.EntityValidateMode.beforeSave) {
            return true;
        }

        // Check twinClassId
        if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId())) {
            entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
        }

        // Check historyNotificationRecipientId
        loadHistoryNotificationRecipient(entity);

        // Check notificationSchemaId
        loadNotificationSchema(entity);

        // Check notificationChannelEventId
        loadNotificationChannelEvent(entity);

        // Check twinClassFieldId
        if (entity.getTwinClassFieldId() != null) {
            if (entity.getTwinClassField() == null || !entity.getTwinClassField().getId().equals(entity.getTwinClassFieldId())) {
                entity.setTwinClassField(twinClassFieldService.findEntitySafe(entity.getTwinClassFieldId()));
            }
        }

        // Check twinValidatorSetId
        if (entity.getTwinValidatorSetId() != null) {
            if (entity.getTwinValidatorSet() == null || !entity.getTwinValidatorSet().getId().equals(entity.getTwinValidatorSetId())) {
                entity.setTwinValidatorSet(twinValidatorSetService.findEntitySafe(entity.getTwinValidatorSetId()));
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
            if (entity.getActive() == null) {
                entity.setActive(true);
            }
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
            updateEntityFieldByValue(sourceEntity.getActive(), entity, HistoryNotificationEntity::getActive, HistoryNotificationEntity::setActive, HistoryNotificationEntity.Fields.active, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    public void loadHistoryNotificationRecipient(HistoryNotificationEntity entity) throws ServiceException {
        loadHistoryNotificationRecipient(List.of(entity));
    }

    public void loadHistoryNotificationRecipient(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        historyNotificationRecipientService.load(entities,
                HistoryNotificationEntity::getHistoryNotificationRecipientId,
                HistoryNotificationEntity::getHistoryNotificationRecipient,
                HistoryNotificationEntity::setHistoryNotificationRecipient);
    }

    public void loadNotificationSchema(HistoryNotificationEntity entity) throws ServiceException {
        loadNotificationSchema(List.of(entity));
    }

    public void loadNotificationSchema(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        notificationSchemaService.load(entities,
                HistoryNotificationEntity::getNotificationSchemaId,
                HistoryNotificationEntity::getNotificationSchema,
                HistoryNotificationEntity::setNotificationSchema);
    }

    public void loadTwinClass(HistoryNotificationEntity entity) throws ServiceException {
        loadTwinClass(List.of(entity));
    }

    public void loadTwinClass(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        twinClassService.load(entities,
                HistoryNotificationEntity::getTwinClassId,
                HistoryNotificationEntity::getTwinClass,
                HistoryNotificationEntity::setTwinClass);
    }

    public void loadTwinClassField(HistoryNotificationEntity entity) throws ServiceException {
        loadTwinClassField(List.of(entity));
    }

    public void loadTwinClassField(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        twinClassFieldService.load(entities,
                HistoryNotificationEntity::getTwinClassFieldId,
                HistoryNotificationEntity::getTwinClassField,
                HistoryNotificationEntity::setTwinClassField);
    }

    public void loadNotificationChannelEvent(HistoryNotificationEntity entity) throws ServiceException {
        loadNotificationChannelEvent(List.of(entity));
    }

    public void loadNotificationChannelEvent(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        notificationEventServiceService.load(entities,
                HistoryNotificationEntity::getNotificationChannelEventId,
                HistoryNotificationEntity::getNotificationChannelEvent,
                HistoryNotificationEntity::setNotificationChannelEvent);
    }

    public void loadTwinValidatorSet(HistoryNotificationEntity entity) throws ServiceException {
        loadTwinValidatorSet(List.of(entity));
    }

    public void loadTwinValidatorSet(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        twinValidatorSetService.load(entities,
                HistoryNotificationEntity::getTwinValidatorSetId,
                HistoryNotificationEntity::getTwinValidatorSet,
                HistoryNotificationEntity::setTwinValidatorSet);
    }

    public void loadCreatedByUser(HistoryNotificationEntity entity) throws ServiceException {
        loadCreatedByUser(List.of(entity));
    }

    public void loadCreatedByUser(Collection<HistoryNotificationEntity> entities) throws ServiceException {
        userService.load(entities,
                HistoryNotificationEntity::getCreatedByUserId,
                HistoryNotificationEntity::getCreatedByUser,
                HistoryNotificationEntity::setCreatedByUser);
    }

    /**
     * Bulk replacement for the per-history getConfigs(...) query in HistoryNotificationTask.
     * One query over the union of (historyTypeId, twinClassId, notificationSchemaId) across all tasks,
     * then in-memory matching via a prebuilt lookup cache (no per-task stream filters).
     *
     * Reproduces the original null-semantics of twinClassFieldId:
     *  - taskFieldId == null  → accept configs with ANY fieldId (old no-fieldId query)
     *  - taskFieldId != null  → exact fieldId match (old with-fieldId query)
     *
     * Used by the batch HistoryNotificationTask run().
     */
    public HistoryNotificationChunk findConfigsForTasks(
            Collection<HistoryNotificationTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks)) {
            return new HistoryNotificationChunk();
        }
        Set<HistoryType> historyTypeIds = null;
        Set<UUID> twinClassIds = null;
        Set<UUID> schemaIds = null;
        UUID domainId = null;
        Map<HistoryNotificationTaskEntity, TaskContext> taskContexts = new HashMap<>();
        for (HistoryNotificationTaskEntity task : tasks) {
            HistoryEntity history = task.getHistory();
            if (history == null || history.getHistoryType() == null
                    || history.getTwin() == null || history.getTwin().getTwinClass() == null) {
                continue;
            }
            HistoryType historyType = history.getHistoryType();
            historyTypeIds = CollectionUtils.safeAdd(historyTypeIds, historyType);
            schemaIds = CollectionUtils.safeAdd(schemaIds, task.getNotificationSchemaId());
            Set<UUID> extended = history.getTwin().getTwinClass().getExtendedClassIdSet();
            Set<UUID> matchingClassIds = extended == null ? new HashSet<>() : new HashSet<>(extended);
            matchingClassIds.add(history.getTwin().getTwinClassId());
            twinClassIds = CollectionUtils.safeAdd(twinClassIds, matchingClassIds);
            if (domainId == null) {
                domainId = history.getTwin().getTwinClass().getDomainId();
            }
            taskContexts.put(task, new TaskContext(historyType, task.getNotificationSchemaId(), matchingClassIds, history.getTwinClassFieldId()));
        }

        if (taskContexts.isEmpty() || historyTypeIds.isEmpty()) {
            return new HistoryNotificationChunk();
        }
        List<HistoryNotificationEntity> candidates = repository.findByHistoryTypeIdInAndTwinClassIdInAndNotificationSchemaIdInAndActiveTrue(
                historyTypeIds, twinClassIds, schemaIds);
        // prebuild lookup cache keyed by HistoryType enum — O(candidates) once, O(1) per (task × classId), no stream filters
        Map<ConfigKey, List<HistoryNotificationEntity>> byKey = new HashMap<>();   // exact fieldId
        Map<ConfigTrioKey, List<HistoryNotificationEntity>> byTrio = new HashMap<>(); // any fieldId
        for (HistoryNotificationEntity c : candidates) {
            HistoryType ht = c.getHistoryTypeId();
            byKey.computeIfAbsent(new ConfigKey(ht, c.getNotificationSchemaId(), c.getTwinClassId(), c.getTwinClassFieldId()), k -> new ArrayList<>()).add(c);
            byTrio.computeIfAbsent(new ConfigTrioKey(ht, c.getNotificationSchemaId(), c.getTwinClassId()), k -> new ArrayList<>()).add(c);
        }
        // match configs to tasks -> config -> set of tasks (single pass)
        Map<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> tasksByConfig = new HashMap<>();
        for (Map.Entry<HistoryNotificationTaskEntity, TaskContext> entry : taskContexts.entrySet()) {
            HistoryNotificationTaskEntity task = entry.getKey();
            TaskContext ctx = entry.getValue();
            for (UUID classId : ctx.matchingClassIds()) {
                List<HistoryNotificationEntity> bucket = ctx.taskFieldId() == null
                        ? byTrio.get(new ConfigTrioKey(ctx.historyTypeId(), ctx.schemaId(), classId))
                        : byKey.get(new ConfigKey(ctx.historyTypeId(), ctx.schemaId(), classId, ctx.taskFieldId()));
                if (bucket != null) {
                    for (HistoryNotificationEntity config : bucket) {
                        tasksByConfig.computeIfAbsent(config, k -> new LinkedHashSet<>()).add(task);
                    }
                }
            }
        }
        return filterByValidation(tasksByConfig, domainId);
    }

    /**
     * Build the chunk (both projections) from the config -> tasks match: preloads all validator sets in
     * one query, validates each config ONLY against the twins of the tasks it matched
     * (TwinValidatorSetService.isValid(Collection<TwinEntity>, config) → Map<twinId, result>), and keeps
     * the (task, config) pairs where the twin passes. Replaces the per-(task, config) isValid(...) call
     * that was inside HistoryNotificationTask.processTask. Requires ApiUser to be set (domainId).
     */
    private HistoryNotificationChunk filterByValidation(
            Map<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> tasksByConfig, UUID domainId) throws ServiceException {
        Map<HistoryNotificationTaskEntity, List<HistoryNotificationEntity>> configsByTask = new HashMap<>();
        Map<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> validTasksByConfig = new HashMap<>();
        if (tasksByConfig.isEmpty()) {
            return new HistoryNotificationChunk(configsByTask, validTasksByConfig, domainId);
        }
        // preload all validator sets in one query; isValid(...) below skips the load since they're cached
        twinValidatorSetService.loadTwinValidatorSet(tasksByConfig.keySet());
        // per config: validate the twins of the tasks it matched, keep (task, config) pairs that pass
        for (Map.Entry<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> entry : tasksByConfig.entrySet()) {
            HistoryNotificationEntity config = entry.getKey();
            LinkedHashSet<HistoryNotificationTaskEntity> tasks = entry.getValue();
            // deduped twins of these tasks (several tasks may share a twin)
            Map<UUID, TwinEntity> twinById = new HashMap<>();
            for (HistoryNotificationTaskEntity task : tasks) {
                TwinEntity twin = task.getHistory().getTwin();
                if (twin != null) {
                    twinById.putIfAbsent(twin.getId(), twin);
                }
            }
            Map<UUID, ValidationResult> validationResults = twinById.isEmpty()
                    ? Collections.emptyMap()
                    : twinValidatorSetService.isValid(twinById.values(), config);
            // tasks that passed validation for this config — keep them in both projections
            LinkedHashSet<HistoryNotificationTaskEntity> validTasks = new LinkedHashSet<>();
            for (HistoryNotificationTaskEntity task : tasks) {
                TwinEntity twin = task.getHistory().getTwin();
                ValidationResult vr = twin != null ? validationResults.get(twin.getId()) : null;
                // keep if valid; defensive keep if the twin/result is undetermined (vr == null)
                if (vr == null || vr.isValid()) {
                    validTasks.add(task);
                    configsByTask.computeIfAbsent(task, k -> new ArrayList<>()).add(config);
                }
            }
            if (!validTasks.isEmpty()) {
                validTasksByConfig.put(config, validTasks);
            }
        }
        return new HistoryNotificationChunk(configsByTask, validTasksByConfig, domainId);
    }

    private record ConfigKey(HistoryType historyTypeId, UUID schemaId, UUID twinClassId, UUID twinClassFieldId) {}
    private record ConfigTrioKey(HistoryType historyTypeId, UUID schemaId, UUID twinClassId) {}
    private record TaskContext(HistoryType historyTypeId, UUID schemaId, Set<UUID> matchingClassIds, UUID taskFieldId) {}
}
