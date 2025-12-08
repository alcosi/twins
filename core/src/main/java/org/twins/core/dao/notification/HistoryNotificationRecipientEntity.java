package org.twins.core.dao.notification;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.featurer.storager.Storager;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "history_notification_recipient")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class HistoryNotificationRecipientEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "recipient_resolver_featurer_id")
    private UUID recipientResolverFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "recipient_resolver_params", columnDefinition = "hstore")
    private HashMap<String, String> recipientResolverParams;

    @FeaturerList(type = RecipientResolver.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_resolver_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity recipientResolverFeaturer;

    public String easyLog(Level level) {
        return "historyNotificationRecipient[id:" + id + "]";
    }
}
