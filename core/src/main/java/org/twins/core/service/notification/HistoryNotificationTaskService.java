package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.service.history.HistoryService;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryNotificationTaskService extends EntitySecureFindServiceImpl<HistoryNotificationTaskEntity> {
    private final HistoryNotificationTaskRepository repository;
    private final HistoryService historyService;
    private final NotificationSchemaService notificationSchemaService;

    @Override
    public CrudRepository<HistoryNotificationTaskEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationTaskEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationTaskEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationTaskEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationTaskEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadHistory(HistoryNotificationTaskEntity entity) throws ServiceException {
        loadHistory(Collections.singleton(entity));
    }

    /**
     * Persists the tasks' status fields with grouped bulk updates — one UPDATE ... WHERE id IN per
     * distinct (statusId, statusDetails, doneAt, attemptCount) tuple — instead of saveAll, which merges
     * every detached entity with a SELECT + UPDATE pair (hundreds of atomic queries per chunk).
     * Callers must set the status fields on the in-memory entities themselves before calling this
     * (entities are detached — see the claim in SchedulerHistoryNotificationTaskRunner.collectAll).
     * REQUIRED propagation: joins the caller's transaction (e.g. the base scheduler claim tx).
     */
    @Transactional(rollbackFor = Throwable.class)
    public void updateStatuses(Collection<HistoryNotificationTaskEntity> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        updateStatusesGrouped(tasks);
    }

    private void updateStatusesGrouped(Collection<HistoryNotificationTaskEntity> tasks) {
        Map<StatusTuple, List<HistoryNotificationTaskEntity>> groups = new HashMap<>();
        for (HistoryNotificationTaskEntity task : tasks) {
            if (task.getStatusId() == null) {
                throw new IllegalArgumentException("statusId must be set before updating " + task.logDetailed());
            }
            StatusTuple tuple = new StatusTuple(
                    task.getStatusId(),
                    task.getStatusDetails(),
                    task.getDoneAt(),
                    task.getAttemptCount() == null ? 0 : task.getAttemptCount());
            groups.computeIfAbsent(tuple, k -> new ArrayList<>()).add(task);
        }
        for (Map.Entry<StatusTuple, List<HistoryNotificationTaskEntity>> entry : groups.entrySet()) {
            StatusTuple tuple = entry.getKey();
            List<HistoryNotificationTaskEntity> group = entry.getValue();
            repository.updateStatusByIdIn(
                    group.stream().map(HistoryNotificationTaskEntity::getId).toList(),
                    tuple.statusId(),
                    tuple.statusDetails(),
                    tuple.doneAt(),
                    tuple.attemptCount());
        }
    }

    private record StatusTuple(HistoryNotificationTaskStatus statusId, String statusDetails, Timestamp doneAt, Integer attemptCount) {
    }

    /**
     * Unsafe (no permission check / no validation) history load: runs in the scheduler thread without an
     * ApiUser / request context, where the secure path (HistoryService.isEntityReadDenied → loadTwin →
     * TwinService.isEntityReadDenied → AuthService.getApiUser on a request-scoped proxy) throws. Domain
     * isolation is enforced later by the notification chunk grouping (one chunk = one domain).
     */
    public void loadHistory(Collection<HistoryNotificationTaskEntity> entities) throws ServiceException {
        historyService.loadUnsafe(entities,
                HistoryNotificationTaskEntity::getHistoryId,
                HistoryNotificationTaskEntity::getHistory,
                HistoryNotificationTaskEntity::setHistory);
    }

    public void loadNotificationSchema(HistoryNotificationTaskEntity entity) throws ServiceException {
        loadNotificationSchema(Collections.singleton(entity));
    }

    public void loadNotificationSchema(Collection<HistoryNotificationTaskEntity> entities) throws ServiceException {
        notificationSchemaService.load(entities,
                HistoryNotificationTaskEntity::getNotificationSchemaId,
                HistoryNotificationTaskEntity::getNotificationSchema,
                HistoryNotificationTaskEntity::setNotificationSchema);
    }
}
