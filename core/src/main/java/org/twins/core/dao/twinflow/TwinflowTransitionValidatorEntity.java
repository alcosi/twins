package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.twins.core.dao.twin.TwinValidatorEntity;

import java.util.*;

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

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private List<TwinValidatorEntity> twinValidators;


    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinflowTransitionValidator[" + id + "]";
            case NORMAL ->
                    "twinflowTransitionValidator[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + "]";
            default ->
                    "twinflowTransitionValidator[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", order:" + order + "]";
        };
    }


    @Override
    public TwinflowTransitionValidatorEntity clone() {
        return new TwinflowTransitionValidatorEntity()
                .setTwinflowTransitionId(twinflowTransitionId)
                .setOrder(order)
                .setTwinValidatorSetId(twinValidatorSetId)
                .setTwinValidators(twinValidators);
    }
}
