package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a rule that can overwrite (auto–calculate) the value or a parameter of a dependent
 * {@link org.twins.core.dao.twinclass.TwinClassFieldEntity} depending on the value/parameter of another field.
 * <p>
 * The rule itself only stores the meta-information that is common for the whole rule.
 * Particular comparisons that should be evaluated are stored in
 * {@link TwinClassFieldConditionEntity} records that reference this rule via {@code rule_id}.
 * </p>
 */
@Entity
@Table(name = "twin_class_field_rule")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldRuleEntity implements EasyLoggable {

    /**
     * Primary key
     */
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    /**
     * Field that will be affected when the rule is triggered.
     */
    @Column(name = "dependent_twin_class_field_id")
    private UUID dependentTwinClassFieldId;

    /**
     * Part of the base field that should be compared – its stored value or one of its parameters.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_twin_class_field_element_type_id")
    private TwinClassFieldConditionElementType targetTwinClassFieldElementTypeId;

    /**
     * Parameter key that has to be checked when {@link #targetTwinClassFieldElementTypeId} is {@link TwinClassFieldConditionElementType#param}.
     */
    @Column(name = "target_param_key")
    private String targetParamKey;

    /**
     * Value that will be assigned to the dependent field (or its parameter) if all conditions evaluate to TRUE.
     * todo - what in case of a sublist?
     */
    @Column(name = "dependent_overwritten_value")
    private String dependentOverwrittenValue;

    @Column(name = "dependent_overwritten_datalist_id")
    private UUID dependentOverwrittenDatalistId;

    /**
     * Priority of the rule – lower value means the rule will be evaluated earlier.
     */
    @Column(name = "rule_priority")
    private Integer rulePriority;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependent_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity dependentTwinClassField;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_rule_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<TwinClassFieldConditionEntity> conditions;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldRule[" + id + "]";
            default -> "twinClassFieldRule[id:" + id + ", dependentTwinClassFieldId:" + dependentTwinClassFieldId + "]";
        };
    }
}
