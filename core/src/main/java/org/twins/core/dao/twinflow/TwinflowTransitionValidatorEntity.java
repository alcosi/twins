package org.twins.core.dao.twinflow;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinflow_transition_validator")
@Accessors(chain = true)
@FieldNameConstants
public class TwinflowTransitionValidatorEntity implements EasyLoggable, PublicCloneable<TwinflowTransitionValidatorEntity> {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "invert")
    private boolean invert;

    @Column(name = "twin_validator_featurer_id")
    private Integer twinValidatorFeaturerId;

    @FeaturerList(type = TwinValidator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinValidatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> twinValidatorParams;

    @Column(name = "active")
    private boolean isActive;

    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinflowTransitionValidator[" + id + "]";
            case NORMAL ->
                    "twinflowTransitionValidator[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", isActive: " + isActive + "]";
            default ->
                    "twinflowTransitionValidator[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", order:" + order + ", featurer:" + twinValidatorFeaturerId + ", isActive: " + isActive + ", invert: " + invert + "]";
        };
    }


    @Override
    public TwinflowTransitionValidatorEntity clone() {
        return new TwinflowTransitionValidatorEntity()
                .setTwinflowTransitionId(twinflowTransitionId)
                .setOrder(order)
                .setInvert(invert)
                .setTwinValidatorFeaturerId(twinValidatorFeaturerId)
                .setTwinValidatorFeaturer(twinValidatorFeaturer)
                .setTwinValidatorParams(twinValidatorParams)
                .setActive(isActive);
    }
}
