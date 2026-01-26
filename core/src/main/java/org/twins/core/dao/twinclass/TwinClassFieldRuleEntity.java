package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;

import java.util.HashMap;
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
    private UUID id;

    /**
     * Value that will be assigned to the dependent field (or its parameter) if all conditions evaluate to TRUE.
     * todo - what in case of a sublist?
     */
    @Column(name = "overwritten_value")
    private String overwrittenValue;

    @Column(name = "overwritten_required", nullable = false)
    private Boolean overwrittenRequired; //not a primitive type because the update logic will break

    /**
     * Priority of the rule – lower value means the rule will be evaluated earlier.
     */
    @Column(name = "rule_priority")
    private Integer rulePriority;

    @Column(name = "field_overwriter_featurer_id")
    private Integer fieldOverwriterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_overwriter_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldOverwriterParams;


    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldConditionEntity, UUID> conditionKit;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldEntity, UUID> fieldKit;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            default -> "twinClassFieldRule[id:" + id + "]";
        };
    }
}
