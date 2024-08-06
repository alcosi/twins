package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Table(name = "twin_factory_multiplier_filter")
@Accessors(chain = true)
@Data
public class TwinFactoryMultiplierFilterEntity implements EasyLoggable {

    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_multiplier_id")
    private UUID twinFactoryMultiplierId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private boolean active;

    @Column(name = "comment")
    private String comment;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryMultiplierFilter[" + id + "]";
            case NORMAL -> "twinFactoryMultiplierFilter[id:" + id + ", twinFactoryMultiplierId:" + twinFactoryMultiplierId + "]";
            default -> "**" + comment + "** twinFactoryMultiplierFilter[id:" + id + ", twinFactoryMultiplierId:" + twinFactoryMultiplierId + ", twinFactoryConditionSetId:" + twinFactoryConditionSetId + ", invert:" + twinFactoryConditionInvert + "]";
        };
    }
}
