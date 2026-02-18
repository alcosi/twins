package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "history_notification")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class HistoryNotificationEntity implements EasyLoggable, ContainsTwinValidatorSet {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "history_type_id")
    private String historyTypeId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Column(name = "twin_validator_set_invert")
    private Boolean twinValidatorSetInvert;

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @Column(name = "history_notification_recipient_id")
    private UUID historyNotificationRecipientId;

    @Column(name = "notification_channel_event_id")
    private UUID notificationChannelEventId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "history_type_id", insertable = false, updatable = false)
    private HistoryTypeEntity historyType;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TwinValidatorSetEntity twinValidatorSet;

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
        return "historyNotification[id:" + id + "]";
    }
}
