package org.twins.core.dao.twinclassfieldrecompute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_class_field_recompute_on_action")
@FieldNameConstants
public class TwinClassFieldRecomputeOnActionEntity implements EasyLoggable, Identifiable {

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
    @JoinColumn(name = "publisher_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity publisherTwinClassSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity subscriberTwinPointer;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity subscriberTwinClassField;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity publisherTwinClass;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldRecomputeOnAction[" + id + "]";
            case NORMAL -> "twinClassFieldRecomputeOnAction[id:" + id
                    + ", publisherClass:" + publisherTwinClassId
                    + ", action:" + publisherTwinAction + "]";
            default -> "twinClassFieldRecomputeOnAction[id:" + id
                    + ", domainId:" + domainId
                    + ", subscriberPointer:" + subscriberTwinPointerId
                    + ", subscriberField:" + subscriberTwinClassFieldId
                    + ", publisherClass:" + publisherTwinClassId
                    + ", action:" + publisherTwinAction
                    + ", async:" + async + "]";
        };
    }
}
