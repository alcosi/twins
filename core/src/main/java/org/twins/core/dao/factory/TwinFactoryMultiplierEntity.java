package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.factory.multiplier.Multiplier;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_factory_multiplier")
public class TwinFactoryMultiplierEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "multiplier_featurer_id")
    private Integer multiplierFeaturerId;

    @Column(name = "description")
    private String description;

    @FeaturerList(type = Multiplier.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "multiplier_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity multiplierFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "multiplier_params", columnDefinition = "hstore")
    private HashMap<String, String> multiplierParams;

    @Column(name = "active")
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClass;

    @Transient
    private Integer factoryMultiplierFiltersCount;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryMultiplier[" + id + "]";
            case NORMAL -> "twinFactoryMultiplier[id:" + id + ", class:" + multiplierFeaturer.getName() + "]";
            default -> "**" + description + "** twinFactoryMultiplier[id:" + id + ", class:" + multiplierFeaturer.getName() + ", twinFactoryId:" + twinFactoryId + "]";
        };
    }
}
