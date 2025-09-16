package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.hibernate.annotations.BatchSize;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_pointer_validator_rule")
public class TwinPointerValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_pointer_id")
    private UUID twinPointerId;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_id", insertable = false, updatable = false)
    private TwinPointerEntity twinPointer;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_set_id", referencedColumnName = "twin_validator_set_id", insertable = false, updatable = false)
    @BatchSize(size = 20)
    private Set<TwinValidatorEntity> twinValidators;

    @Transient
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
