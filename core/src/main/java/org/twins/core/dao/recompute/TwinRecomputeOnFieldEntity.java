package org.twins.core.dao.recompute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

/**
 * OnField recompute rule: "when publisher field changes, recompute the subscriber via {@link #subscriber}".
 * Subscriber side (pointer, field, recomputer) lives on the {@link TwinRecomputeSubscriberEntity} parent.
 */
@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_recompute_on_field")
@FieldNameConstants
public class TwinRecomputeOnFieldEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "recompute_subscriber_id", nullable = false)
    private UUID recomputeSubscriberId;

    @Column(name = "publisher_twin_class_field_id", nullable = false)
    private UUID publisherTwinClassFieldId;

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
    @JoinColumn(name = "publisher_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity publisherTwinClassFieldSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinRecomputeSubscriberEntity subscriber;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity publisherTwinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinRecomputeOnField[" + id + "]";
            case NORMAL -> "twinRecomputeOnField[id:" + id
                    + ", subscriber:" + recomputeSubscriberId
                    + ", publisherField:" + publisherTwinClassFieldId + "]";
            default -> "twinRecomputeOnField[id:" + id
                    + ", subscriber:" + recomputeSubscriberId
                    + ", publisherField:" + publisherTwinClassFieldId
                    + ", async:" + async + "]";
        };
    }
}
