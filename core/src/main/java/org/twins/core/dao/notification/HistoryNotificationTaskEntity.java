package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "history_notification_task")
@Data
@FieldNameConstants
@Accessors(chain = true)
public class HistoryNotificationTaskEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "history_id")
    private UUID historyId;

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @Column(name = "history_notification_task_status_id")
    @Enumerated(EnumType.STRING)
    private HistoryNotificationTaskStatus statusId;

    @Column(name = "status_details")
    private String statusDetails;

    /**
     * Consecutive infra-level failures of this task: incremented when the notification chunk fails before
     * the task was processed (bulk-stage failure in {@code HistoryNotificationTask}) or when its chunk
     * submission was rejected (see {@code SchedulerHistoryNotificationTaskRunner.revertStatusAndSave}).
     * After {@code HistoryNotificationTask.MAX_BATCH_ATTEMPTS} consecutive failures the task is
     * terminally failed (poison-pill protection against an infinite retry loop). Business errors of the
     * task itself fail terminally on the first attempt and never increment this counter.
     */
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    /**
     * DB-owned: set by the history_notification_task_touch_done_at trigger on the transition INTO
     * SENT (per-row clock_timestamp precision — bulk updates fire row triggers). The application
     * never writes it: updatable = false here, the bulk status update does not include the column.
     */
    @Column(name = "done_at", updatable = false)
    private Timestamp doneAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", insertable = false, updatable = false)
    private HistoryEntity historySpecOnly;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private HistoryEntity history;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_schema_id", insertable = false, updatable = false)
    private NotificationSchemaEntity notificationSchemaSpecOnly;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private NotificationSchemaEntity notificationSchema;

    /**
     * Transient-runtime cache: recipientIds resolved per recipient for this task's history.
     * Populated once per chunk by {@code HistoryNotificationRecipientService.resolveRecipientsBatch}
     * (chunk-level batch resolve, grouped by {@code (resolverFeaturerId, canonical params)}), then read
     * by {@code HistoryNotificationTask.processTask}. Never persisted, never crosses a chunk run.
     */
    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Map<UUID, Set<UUID>> resolvedRecipientsByRecipientId;

    /**
     * Transient-runtime cache: notification context collected per contextId for this task's history.
     * Populated once per chunk by {@code NotificationContextService.collectHistoryContextBatch}
     * (chunk-level batch collection, i18n resolved per locale), then read by {@code HistoryNotificationTask.processTask}.
     * Never persisted, never crosses a chunk run.
     */
    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Map<UUID, Map<String, String>> collectedContextByContextId;

    public String easyLog(Level level) {
        return "historyNotificationTaskEntity[id:" + id + "]";
    }
}

