package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory_multiplier_filter")
public class TwinFactoryMultiplierFilterEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "twin_factory_multiplier_id")
    private UUID twinFactoryMultiplierId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private boolean active;

    @Column(name = "description")
    private String description;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_multiplier_id", insertable = false, updatable = false)
    private TwinFactoryMultiplierEntity multiplierSpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity conditionSetSpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClassSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinFactoryMultiplierEntity multiplier;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity inputTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinFactoryConditionSetEntity conditionSet;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryMultiplierFilter[" + id + "]";
            case NORMAL ->
                    "twinFactoryMultiplierFilter[id:" + id + ", twinFactoryMultiplierId:" + twinFactoryMultiplierId + "]";
            default ->
                    "**" + description + "** twinFactoryMultiplierFilter[id:" + id + ", twinFactoryMultiplierId:" + twinFactoryMultiplierId + ", twinFactoryConditionSetId:" + twinFactoryConditionSetId + ", invert:" + twinFactoryConditionInvert + "]";
        };
    }
}
