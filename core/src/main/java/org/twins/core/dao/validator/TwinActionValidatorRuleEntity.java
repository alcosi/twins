package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.enums.action.TwinAction;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_action_validator_rule")
@Accessors(chain = true)
public class TwinActionValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAction twinAction;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinActionValidatorRule[" + id + "]";
            case NORMAL -> "twinActionValidatorRule[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinActionValidatorRule[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
