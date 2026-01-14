package org.twins.core.dao.factory;

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

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "twin_factory_condition")
@Accessors(chain = true)
@Data
@FieldNameConstants
public class TwinFactoryConditionEntity implements EasyLoggable {
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "conditioner_featurer_id")
    private Integer conditionerFeaturerId;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "invert")
    private Boolean invert;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity conditionerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "conditioner_params", columnDefinition = "hstore")
    private HashMap<String, String> conditionerParams;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity conditionSet;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryCondition[" + id + "]";
            default -> "twinFactoryCondition[id:" + id + ", conditionerFeaturerId:" + conditionerFeaturerId + "]";
        };
    }
}
