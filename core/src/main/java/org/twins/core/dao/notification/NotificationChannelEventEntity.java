package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Data
@DynamicUpdate
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "notification_channel_event")
public class NotificationChannelEventEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "notification_channel_id")
    private UUID notificationChannelId;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "history_notification_context_id")
    private UUID historyNotificationContextId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_channel_id", insertable = false, updatable = false)
    private NotificationChannelEntity notificationChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_notification_context_id", insertable = false, updatable = false)
    private HistoryNotificationContextEntity historyNotificationContext;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "notificationChannelEvent[" + id + "]";
            case NORMAL -> "notificationChannelEvent[id:" + id + ", eventCode:" + eventCode + "]";
            default -> "notificationChannelEvent[id:" + id + "]";
        };
    }
}

