package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.dispatcher.Dispatcher;

import java.util.Map;
import java.util.UUID;

// todo change doc
/**
 * Mapping for <pre>domain_subscription_event_type</pre> table. Composite primary key consists of
 * - domain_id (UUID)
 * - subscription_event_type_id (varchar / enum name)
 * <p>
 * todo - maybe, this table is not needed, if all events are from the history table,
 * then history_type_domain_template entity can be used for these purposes
 */
@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_subscription_event")
public class DomainSubscriptionEventEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "subscription_event_type_id")
    @Enumerated(EnumType.STRING)
    private SubscriptionEventType subscriptionEventTypeId;

    @Column(name = "dispatcher_featurer_id")
    private Integer dispatcherFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "dispatcher_featurer_params", columnDefinition = "hstore")
    private Map<String, String> dispatcherFeaturerParams;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @FeaturerList(type = Dispatcher.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatcher_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity dispatcherFeaturer;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL ->
                    "domainSubscriptionEventType[domain:" + domainId + ", eventType:" + subscriptionEventTypeId + "]";
            default -> "domainSubscriptionEventType[domain:" + domainId + "]";
        };
    }
}
