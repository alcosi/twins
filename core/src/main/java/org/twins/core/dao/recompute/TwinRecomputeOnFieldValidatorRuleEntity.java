package org.twins.core.dao.recompute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_recompute_on_field_validator_rule")
@FieldNameConstants
public class TwinRecomputeOnFieldValidatorRuleEntity implements EasyLoggable, Identifiable, ContainsTwinValidatorSet {

    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_recompute_on_field_id", nullable = false)
    private UUID twinRecomputeOnFieldId;

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
    @JoinColumn(name = "twin_recompute_on_field_id", insertable = false, updatable = false)
    private TwinRecomputeOnFieldEntity twinRecomputeOnFieldSpecOnly;

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
    private TwinRecomputeOnFieldEntity twinRecomputeOnField;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinValidatorSetEntity twinValidatorSet;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinRecomputeOnFieldValidatorRule[" + id + "]";
            case NORMAL -> "twinRecomputeOnFieldValidatorRule[id:" + id
                    + ", onField:" + twinRecomputeOnFieldId
                    + ", order:" + order + "]";
            default -> "twinRecomputeOnFieldValidatorRule[id:" + id
                    + ", onField:" + twinRecomputeOnFieldId
                    + ", order:" + order
                    + ", active:" + active
                    + ", validatorSet:" + twinValidatorSetId + "]";
        };
    }
}
