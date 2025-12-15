package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

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

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @Column(name = "history_notification_recipient_id")
    private UUID historyNotificationRecipientId;

    @Column(name = "notification_channel_event_id")
    private UUID notificationChannelEventId;

    @ManyToOne
    @JoinColumn(name = "history_type_id", insertable = false, updatable = false)
    private HistoryTypeEntity historyType;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "notification_schema_id", insertable = false, updatable = false)
    private NotificationSchemaEntity notificationSchema;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_notification_recipient_id", insertable = false, updatable = false)
    private HistoryNotificationRecipientEntity historyNotificationRecipient;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "notification_channel_event_id", insertable = false, updatable = false)
    private NotificationChannelEventEntity notificationChannelEvent;

    public String easyLog(Level level) {
        return "historyNotificationSchemaMap[id:" + id + "]";
    }
}
