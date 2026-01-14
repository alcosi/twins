package org.twins.core.dao.notification;

import com.github.f4b6a3.uuid.UuidCreator;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "notification_context_collector")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class NotificationContextCollectorEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "notification_context_id")
    private UUID notificationContextId;

    @Column(name = "context_collector_featurer_id")
    private Integer contextCollectorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "context_collector_params", columnDefinition = "hstore")
    private HashMap<String, String> contextCollectorParams;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "notification_context_id", insertable = false, updatable = false)
    private NotificationContextEntity historyNotificationContext;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Transient
    private FeaturerEntity contextCollectorFeaturer;

    public String easyLog(Level level) {
        return "notificationContextCollector[id:" + id + "]";
    }
}

