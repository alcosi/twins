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
@FieldNameConstants
@Accessors(chain = true)
public class TwinflowTransitionValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable, PublicCloneable<TwinflowTransitionValidatorRuleEntity> {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    //TODO think over @ManyToMany https://alcosi.atlassian.net/browse/TWINS-220
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", referencedColumnName = "twin_validator_set_id", insertable = false, updatable = false)
    private Set<TwinValidatorEntity> twinValidators;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinflowTransitionValidatorRule[" + id + "]";
            case NORMAL ->
                    "twinflowTransitionValidatorRule[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + "]";
            default ->
                    "twinflowTransitionValidatorRule[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", order:" + order + "]";
        };
    }


    @Override
    public TwinflowTransitionValidatorRuleEntity clone() {
        TwinflowTransitionValidatorRuleEntity newEntity = new TwinflowTransitionValidatorRuleEntity();
        newEntity.setTwinflowTransitionId(twinflowTransitionId);
        newEntity.setOrder(order);
        newEntity.setTwinValidatorSetId(twinValidatorSetId);
        newEntity.setTwinValidators(twinValidators);
        return newEntity;
    }
}
