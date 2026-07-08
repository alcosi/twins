package org.twins.core.dao.twinclassfieldrecompute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_class_field_recompute_on_action_validator_rule")
@FieldNameConstants
public class TwinClassFieldRecomputeOnActionValidatorRuleEntity implements EasyLoggable, Identifiable {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_field_recompute_on_action_id", nullable = false)
    private UUID twinClassFieldRecomputeOnActionId;

    @Column(name = "\"order\"")
    private Integer order;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_recompute_on_action_id", insertable = false, updatable = false)
    private TwinClassFieldRecomputeOnActionEntity twinClassFieldRecomputeOnActionSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private TwinValidatorSetEntity twinValidatorSetSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldRecomputeOnActionEntity twinClassFieldRecomputeOnAction;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinValidatorSetEntity twinValidatorSet;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldRecomputeValidatorRule[" + id + "]";
            case NORMAL -> "twinClassFieldRecomputeValidatorRule[id:" + id
                    + ", recompute:" + twinClassFieldRecomputeOnActionId
                    + ", order:" + order + "]";
            default -> "twinClassFieldRecomputeValidatorRule[id:" + id
                    + ", recompute:" + twinClassFieldRecomputeOnActionId
                    + ", order:" + order
                    + ", active:" + active
                    + ", validatorSet:" + twinValidatorSetId + "]";
        };
    }
}
