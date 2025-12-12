package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_pointer_validator_rule")
public class TwinPointerValidatorRuleEntity implements ContainsTwinValidatorSet {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_pointer_id")
    private UUID twinPointerId;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_id", insertable = false, updatable = false)
    private TwinPointerEntity twinPointer;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private TwinValidatorSetEntity twinValidatorSet;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinPointerValidatorRule[" + id + "]";
            default:
                return "twinPointerValidatorRule[id:" + id + ", twinPointerId:" + twinPointerId + "]";
        }
    }
}
