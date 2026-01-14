package org.twins.core.dao.factory;

import com.github.f4b6a3.uuid.UuidCreator;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_factory_multiplier")
public class TwinFactoryMultiplierEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "multiplier_featurer_id")
    private Integer multiplierFeaturerId;

    @Column(name = "description")
    private String description;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity multiplierFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "multiplier_params", columnDefinition = "hstore")
    private HashMap<String, String> multiplierParams;

    @Column(name = "active")
    private Boolean active;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClass;

    @Transient
    private Integer factoryMultiplierFiltersCount;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryMultiplier[" + id + "]";
            case NORMAL -> "twinFactoryMultiplier[id:" + id + ", multiplierFeaturerId:" + multiplierFeaturerId + "]";
            default -> "**" + description + "** twinFactoryMultiplier[id:" + id + ", multiplierFeaturerId:" + multiplierFeaturerId + ", twinFactoryId:" + twinFactoryId + "]";
        };
    }
}
