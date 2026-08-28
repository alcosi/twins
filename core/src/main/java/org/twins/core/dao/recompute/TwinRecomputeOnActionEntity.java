package org.twins.core.dao.recompute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

/**
 * OnAction recompute rule: "when a twin of publisher class undergoes an action, recompute the subscriber
 * via {@link #subscriber}". Subscriber side lives on the {@link TwinRecomputeSubscriberEntity} parent.
 */
@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_recompute_on_action")
@FieldNameConstants
public class TwinRecomputeOnActionEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "recompute_subscriber_id", nullable = false)
    private UUID recomputeSubscriberId;

    @Column(name = "publisher_twin_class_id", nullable = false)
    private UUID publisherTwinClassId;

    @Column(name = "publisher_twin_action_id", nullable = false)
    @Enumerated(EnumType.STRING)
    private TwinAction publisherTwinAction;

    @Column(name = "async", nullable = false)
    private boolean async;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recompute_subscriber_id", insertable = false, updatable = false)
    private TwinRecomputeSubscriberEntity recomputeSubscriberSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity publisherTwinClassSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinRecomputeSubscriberEntity subscriber;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity publisherTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinRecomputeOnActionValidatorRuleEntity, UUID> validatorRulesKit;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinRecomputeOnAction[" + id + "]";
            case NORMAL -> "twinRecomputeOnAction[id:" + id
                    + ", publisherClass:" + publisherTwinClassId
                    + ", action:" + publisherTwinAction + "]";
            default -> "twinRecomputeOnAction[id:" + id
                    + ", subscriber:" + recomputeSubscriberId
                    + ", publisherClass:" + publisherTwinClassId
                    + ", action:" + publisherTwinAction
                    + ", async:" + async + "]";
        };
    }
}
