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

import java.util.HashMap;
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
    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassField;

    @Column(name = "field_overwriter_featurer_id")
    private Integer fieldOverwriterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_overwriter_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldOverwriterParams;


    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_rule_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<TwinClassFieldConditionEntity> conditions;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldRule[" + id + "]";
            default -> "twinClassFieldRule[id:" + id + ", dependentTwinClassFieldId:" + twinClassFieldId + "]";
        };
    }
}
