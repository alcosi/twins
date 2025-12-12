package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.sql.Timestamp;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "history_id", insertable = false, updatable = false)
    private HistoryEntity history;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "notification_schema_id", insertable = false, updatable = false)
    private NotificationSchemaEntity notificationSchema;

    public String easyLog(Level level) {
        return "historyNotificationTaskEntity[id:" + id + "]";
    }
}

