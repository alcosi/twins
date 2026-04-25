package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.action.TwinClassFieldAction;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_field_action_validation_rule")
@Accessors(chain = true)
public class TwinClassFieldActionValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "twin_class_field_action_id")
    @Enumerated(EnumType.STRING)
    private TwinClassFieldAction twinClassFieldAction;

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

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldActionValidatorRule[" + id + "]";
            case NORMAL -> "twinClassFieldActionValidatorRule[id:" + id + ", twinClassFieldId:" + twinClassFieldId + "]";
            default ->
                    "twinClassFieldActionValidatorRule[id:" + id + ", twinClassFieldId:" + twinClassFieldId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
