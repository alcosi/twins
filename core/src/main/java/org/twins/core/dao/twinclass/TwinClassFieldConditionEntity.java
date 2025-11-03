package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.enums.twinclass.LogicOperator;

import java.util.HashMap;
import java.util.UUID;

/**
 * Single comparison that belongs to a {@link TwinClassFieldRuleEntity}.
 * <p>
 * All conditions with the same {@link #groupNo} are combined with logical AND, different groups are OR-ed.
 * Grouping allows you to build complex logical expressions with brackets.
 * </p>
 */
@Entity
@Table(name = "twin_class_field_condition")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldConditionEntity implements EasyLoggable {

    /**
     * Primary key
     */
    @Id
    private UUID id;

    /**
     * Parent rule
     */
    @Column(name = "twin_class_field_rule_id")
    private UUID twinClassFieldRuleId;

    /**
     * Base field we compare against.
     */
    @Column(name = "base_twin_class_field_id")
    private UUID baseTwinClassFieldId;

    /**
     * Order of the condition inside the rule (is used only for readability/UI)
     */
    @Column(name = "condition_order")
    private Integer conditionOrder;

    /**
     * Group number – conditions with the same group are AND-ed, different groups OR-ed
     */
    @Column(name = "group_no")
    private Integer groupNo = 1;

    @Column(name = "parent_twin_class_field_condition_id")
    private UUID parentTwinClassFieldConditionId;

    @Column(name = "logic_operator_id")
    private LogicOperator logicOperatorId;

    @Column(name = "condition_evaluator_featurer_id")
    private Integer conditionEvaluatorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "condition_evaluator_params", columnDefinition = "hstore")
    private HashMap<String, String> conditionEvaluatorParams;


    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "twin_class_field_rule_id", insertable = false, updatable = false)
    private TwinClassFieldRuleEntity twinClassFieldRule;

    @Transient
    private TwinClassFieldEntity baseTwinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldCondition[" + id + "]";
            default ->
                    "twinClassFieldCondition[id:" + id + ", ruleId:" + twinClassFieldRuleId + ", baseFieldId:" + baseTwinClassFieldId + "]";
        };
    }
}
