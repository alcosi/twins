package org.twins.core.dao.validator;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Table(name = "twin_validator")
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Column(name = "twin_validator_featurer_id")
    private Integer twinValidatorFeaturerId;

    @FeaturerList(type = TwinValidator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinValidatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> twinValidatorParams;

    @Column(name = "invert")
    private boolean invert;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "description")
    private String description;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinValidatorEntity[" + id + "]";
            case NORMAL -> "twinValidatorEntity[id:" + id + ", twinValidatorSetId:" + twinValidatorSetId + "]";
            default ->
                    "twinValidatorEntity[id:" + id + ", twinValidatorSetId:" + twinValidatorSetId + ", twinValidatorFeaturerId:" + twinValidatorFeaturerId + "]";
        };
    }

}
