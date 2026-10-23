package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.context.ContextCollectorEntity;
import org.twins.core.dao.context.ContextEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Entity
@Data
@DynamicUpdate
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "notification_channel_event")
public class NotificationChannelEventEntity implements EasyLoggable, Identifiable<UUID> {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "notification_channel_id")
    private UUID notificationChannelId;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "context_id")
    private UUID contextId;

    @Column(name = "unique_in_batch")
    private boolean uniqueInBatch = false;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_channel_id", insertable = false, updatable = false)
    private NotificationChannelEntity notificationChannelSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "context_id", insertable = false, updatable = false)
    private ContextEntity contextSpecOnly;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private NotificationChannelEntity notificationChannel;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ContextEntity context;

    /**
     * Transient-runtime: context collectors of this event's context (loaded in bulk via
     * {@code loadContextCollectors}). Grouped by contextId, so several events sharing one context share
     * the same collector list.
     */
    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Kit<ContextCollectorEntity, UUID> collectors;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "notificationChannelEvent[" + id + "]";
            case NORMAL -> "notificationChannelEvent[id:" + id + ", eventCode:" + eventCode + "]";
            default -> "notificationChannelEvent[id:" + id + "]";
        };
    }
}
