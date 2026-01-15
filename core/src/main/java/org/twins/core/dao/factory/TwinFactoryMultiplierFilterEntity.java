package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory_multiplier_filter")
public class TwinFactoryMultiplierFilterEntity implements EasyLoggable {
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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_multiplier_id", insertable = false, updatable = false)
    private TwinFactoryMultiplierEntity multiplier;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity conditionSet;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClass;

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
