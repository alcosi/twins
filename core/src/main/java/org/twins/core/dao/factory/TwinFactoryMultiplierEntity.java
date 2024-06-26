package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.factory.multiplier.Multiplier;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "twin_factory_multiplier")
@Accessors(chain = true)
@Data
public class TwinFactoryMultiplierEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "multiplier_featurer_id")
    private int multiplierFeaturerId;

    @Column(name = "comment")
    private String comment;

    @FeaturerList(type = Multiplier.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "multiplier_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity multiplierFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "multiplier_params", columnDefinition = "hstore")
    private HashMap<String, String> multiplierParams;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryMultiplier[" + id + "]";
            case NORMAL -> "twinFactoryMultiplier[id:" + id + ", class:" + multiplierFeaturer.getName() + "]";
            default -> "**" + comment + "** twinFactoryMultiplier[id:" + id + ", class:" + multiplierFeaturer.getName() + ", twinFactoryId:" + twinFactoryId + "]";
        };
    }
}
