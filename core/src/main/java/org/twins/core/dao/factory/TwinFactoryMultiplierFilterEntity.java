package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory_multiplier_filter")
public class TwinFactoryMultiplierFilterEntity implements EasyLoggable {

    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_multiplier_id", insertable = false, updatable = false)
    private TwinFactoryMultiplierEntity multiplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
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
