package org.twins.core.dao.notification;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "history_notification_recipient_collector")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class HistoryNotificationRecipientCollectorEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "history_notification_recipient_id")
    private UUID historyNotificationRecipientId;

    @Column(name = "recipient_resolver_featurer_id")
    private Integer recipientResolverFeaturerId;

    @Column(name = "exclude")
    private Boolean exclude;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "recipient_resolver_params", columnDefinition = "hstore")
    private HashMap<String, String> recipientResolverParams;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity recipientResolverFeaturer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "history_notification_recipient_id", insertable = false, updatable = false)
    private HistoryNotificationRecipientEntity historyNotificationRecipientEntity;

    public String easyLog(Level level) {
        return "historyNotificationRecipientCollector[id:" + id + "]";
    }
}
