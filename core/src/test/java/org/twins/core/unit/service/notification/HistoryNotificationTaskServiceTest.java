package org.twins.core.unit.service.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.notification.HistoryNotificationTaskService;
import org.twins.core.service.notification.NotificationSchemaService;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.same;

/**
 * Grouped bulk persistence of {@code HistoryNotificationTaskService#updateStatuses}: one
 * UPDATE ... WHERE id IN per distinct (statusId, statusDetails, doneAt, attemptCount) tuple —
 * replaces the per-entity merge (SELECT + UPDATE per row) of saveAll for detached task entities.
 */
class HistoryNotificationTaskServiceTest extends BaseUnitTest {

    @Mock
    private HistoryNotificationTaskRepository repository;
    @Mock
    private HistoryService historyService;
    @Mock
    private NotificationSchemaService notificationSchemaService;

    private HistoryNotificationTaskService service;

    @BeforeEach
    void setUp() {
        service = new HistoryNotificationTaskService(repository, historyService, notificationSchemaService);
    }

    private HistoryNotificationTaskEntity task(HistoryNotificationTaskStatus status, String details, Timestamp doneAt, Integer attemptCount) {
        return new HistoryNotificationTaskEntity()
                .setId(UUID.randomUUID())
                .setStatusId(status)
                .setStatusDetails(details)
                .setDoneAt(doneAt)
                .setAttemptCount(attemptCount);
    }

    @Test
    void identicalTuples_oneBulkUpdateForTheGroup() {
        var doneAt = Timestamp.valueOf("2026-08-26 12:00:00");
        var task1 = task(HistoryNotificationTaskStatus.SENT, "5 recipients were notified", doneAt, 0);
        var task2 = task(HistoryNotificationTaskStatus.SENT, "5 recipients were notified", doneAt, 0);

        service.updateStatuses(List.of(task1, task2));

        verify(repository, times(1)).updateStatusByIdIn(
                eq(List.of(task1.getId(), task2.getId())),
                eq(HistoryNotificationTaskStatus.SENT),
                eq("5 recipients were notified"),
                same(doneAt),
                eq(0));
    }

    @Test
    void distinctTuples_oneBulkUpdatePerTuple() {
        var doneAt = Timestamp.valueOf("2026-08-26 12:00:00");
        var sent = task(HistoryNotificationTaskStatus.SENT, "5 recipients were notified", doneAt, 0);
        var skipped = task(HistoryNotificationTaskStatus.SKIPPED, "No configs found", doneAt, 0);

        service.updateStatuses(List.of(sent, skipped));

        verify(repository, times(1)).updateStatusByIdIn(eq(List.of(sent.getId())), eq(HistoryNotificationTaskStatus.SENT), anyString(), same(doneAt), eq(0));
        verify(repository, times(1)).updateStatusByIdIn(eq(List.of(skipped.getId())), eq(HistoryNotificationTaskStatus.SKIPPED), anyString(), same(doneAt), eq(0));
    }

    @Test
    void nullAttemptCount_treatedAsZero() {
        var inProgress = task(HistoryNotificationTaskStatus.IN_PROGRESS, null, null, null);

        service.updateStatuses(List.of(inProgress));

        verify(repository, times(1)).updateStatusByIdIn(anyList(), eq(HistoryNotificationTaskStatus.IN_PROGRESS), isNull(), isNull(), eq(0));
    }

    @Test
    void missingStatusId_failsFast() {
        var noStatus = new HistoryNotificationTaskEntity().setId(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> service.updateStatuses(List.of(noStatus)));
        verifyNoInteractions(repository);
    }
}
