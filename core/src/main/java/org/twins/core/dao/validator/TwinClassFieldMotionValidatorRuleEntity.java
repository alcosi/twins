package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_field_motion_validator_rule")
@FieldNameConstants
@Accessors(chain = true)
public class TwinClassFieldMotionValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable, PublicCloneable<TwinClassFieldMotionValidatorRuleEntity> {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_field_motion_id")
    private UUID fieldMotionId;

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

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "fieldMotionValidatorRule[" + id + "]";
            case NORMAL ->
                    "fieldMotionValidatorRule[id:" + id + ", fieldMotionId:" + fieldMotionId + "]";
            default ->
                    "fieldMotionValidatorRule[id:" + id + ", fieldMotionId:" + fieldMotionId + ", order:" + order + "]";
        };
    }


    @Override
    public TwinClassFieldMotionValidatorRuleEntity clone() {
        return new TwinClassFieldMotionValidatorRuleEntity()
                .setFieldMotionId(fieldMotionId)
                .setOrder(order)
                .setTwinValidatorSetId(twinValidatorSetId)
                .setTwinValidators(twinValidators);
    }
}
