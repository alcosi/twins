package org.twins.core.dao.recompute;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.Identifiable;

import java.util.HashMap;
import java.util.UUID;

/**
 * One recompute subscriber per (twin pointer, twin class field). Carries the {@code Recomputer} featurer
 * config used to recompute the subscriber field when a publisher rule fires. NULL recomputer means the
 * default {@code RecomputerByFieldTyper}. See TWINS-893.
 */
@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_recompute_subscriber")
@FieldNameConstants
public class TwinRecomputeSubscriberEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "subscriber_twin_pointer_id", nullable = false)
    private UUID subscriberTwinPointerId;

    @Column(name = "subscriber_twin_class_field_id", nullable = false)
    private UUID subscriberTwinClassFieldId;

    @Column(name = "recomputer_featurer_id", nullable = false)
    private Integer recomputerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "recomputer_params", columnDefinition = "hstore")
    private HashMap<String, String> recomputerParams;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domainSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_twin_pointer_id", insertable = false, updatable = false)
    private TwinPointerEntity subscriberTwinPointerSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity subscriberTwinClassFieldSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recomputer_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity recomputerFeaturerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity subscriberTwinPointer;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity subscriberTwinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinRecomputeSubscriber[" + id + "]";
            case NORMAL -> "twinRecomputeSubscriber[id:" + id
                    + ", subscriberField:" + subscriberTwinClassFieldId + "]";
            default -> "twinRecomputeSubscriber[id:" + id
                    + ", domainId:" + domainId
                    + ", subscriberPointer:" + subscriberTwinPointerId
                    + ", subscriberField:" + subscriberTwinClassFieldId
                    + ", recomputerFeaturerId:" + recomputerFeaturerId + "]";
        };
    }
}
