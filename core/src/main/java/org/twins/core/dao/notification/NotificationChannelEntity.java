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
@Table(name = "notification_channel")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class NotificationChannelEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "notifier_featurer_id")
    private Integer notifierFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "notifier_params", columnDefinition = "hstore")
    private HashMap<String, String> notifierParams;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity notifierFeaturer;

    public String easyLog(Level level) {
        return "notificationChannel[id:" + id + "]";
    }
}
