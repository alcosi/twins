package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;

import java.util.*;

@Entity
@Data
@Table(name = "twinflow_transition_validator_rule")
@Accessors(chain = true)
@FieldNameConstants
public class TwinflowTransitionValidatorRuleEntity implements Validator, EasyLoggable, PublicCloneable<TwinflowTransitionValidatorRuleEntity> {
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
    public TwinflowTransitionValidatorRuleEntity clone() {
        return new TwinflowTransitionValidatorRuleEntity()
                .setTwinflowTransitionId(twinflowTransitionId)
                .setOrder(order)
                .setTwinValidatorSetId(twinValidatorSetId)
                .setTwinValidators(twinValidators);
    }
}
