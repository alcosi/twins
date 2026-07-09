package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.Identifiable;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "twin_factory_multiplier")
public class TwinFactoryMultiplierEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "multiplier_featurer_id")
    private Integer multiplierFeaturerId;

    @Column(name = "description")
    private String description;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "multiplier_params", columnDefinition = "hstore")
    private HashMap<String, String> multiplierParams;

    @Column(name = "active")
    private Boolean active;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactorySpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClassSpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplier_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity multiplierFeaturerSpecOnly;

    @Transient
    private Integer factoryMultiplierFiltersCount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinFactoryEntity twinFactory;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity inputTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryMultiplierFilterEntity, UUID> twinFactoryMultiplierFilterKit;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryMultiplier[" + id + "]";
            case NORMAL -> "twinFactoryMultiplier[id:" + id + ", multiplierFeaturerId:" + multiplierFeaturerId + "]";
            default ->
                    "**" + description + "** twinFactoryMultiplier[id:" + id + ", multiplierFeaturerId:" + multiplierFeaturerId + ", twinFactoryId:" + twinFactoryId + "]";
        };
    }
}
