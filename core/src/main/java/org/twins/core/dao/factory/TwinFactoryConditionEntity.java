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
@Table(name = "twin_factory_condition")
@Accessors(chain = true)
@Data
public class TwinFactoryConditionEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "conditioner_featurer_id")
    private int conditionerFeaturerId;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean active;

    @Column(name = "invert")
    private boolean invert;

    @FeaturerList(type = Multiplier.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "conditioner_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity conditionerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "conditioner_params", columnDefinition = "hstore")
    private HashMap<String, String> conditionerParams;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryCondition[" + id + "]";
            default -> "twinFactoryCondition[id:" + id + ", conditionerFeaturerId:" + conditionerFeaturerId + "]";
        };
    }
}
