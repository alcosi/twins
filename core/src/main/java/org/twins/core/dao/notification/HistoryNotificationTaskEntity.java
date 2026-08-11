package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "history_notification_task")
@DynamicUpdate
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

    @Column(name = "done_at")
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

    public String easyLog(Level level) {
        return "historyNotificationTaskEntity[id:" + id + "]";
    }
}

