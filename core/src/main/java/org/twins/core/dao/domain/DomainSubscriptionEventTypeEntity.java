package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

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
@Table(name = "domain_subscription_event_type")
public class DomainSubscriptionEventTypeEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "subscription_event_type_id")
    @Enumerated(EnumType.STRING)
    private SubscriptionEventType subscriptionEventTypeId;

    @Column(name = "subscription_enabled")
    private boolean subscriptionEnabled;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL ->
                    "domainSubscriptionEventType[domain:" + domainId + ", eventType:" + subscriptionEventTypeId + "]";
            default -> "domainSubscriptionEventType[domain:" + domainId + "]";
        };
    }
}
