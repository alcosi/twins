package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.history.HistoryTypeEntity;

import java.util.UUID;

@Entity
@Table(name = "history_notification_schema_map")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class HistoryNotificationSchemaMapEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "history_type_id")
    private String historyTypeId;

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @Column(name = "history_notification_recipient_id")
    private UUID historyNotificationRecipientId;

    @Column(name = "notification_channel_event_id")
    private UUID notificationChannelEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_type_id", insertable = false, updatable = false)
    private HistoryTypeEntity historyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_schema_id", insertable = false, updatable = false)
    private NotificationSchemaEntity notificationSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_notification_recipient_id", insertable = false, updatable = false)
    private HistoryNotificationRecipientEntity historyNotificationRecipient;

    @ManyToOne
    @JoinColumn(name = "notification_channel_event_id", insertable = false, updatable = false)
    private NotificationChannelEventEntity notificationChannelEvent;

    public String easyLog(Level level) {
        return "historyNotificationSchemaMap[id:" + id + "]";
    }
}
