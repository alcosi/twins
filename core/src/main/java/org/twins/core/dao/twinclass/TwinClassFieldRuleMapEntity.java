package org.twins.core.dao.twinclass;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Table(name = "twin_class_field_rule_map")
@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldRuleMapEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "twin_class_field_rule_id")
    private UUID twinClassFieldRuleId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "twin_class_field_rule_id", insertable = false, updatable = false)
    private TwinClassFieldRuleEntity twinClassFieldRule;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldRuleMapEntity [id=" + id + "]";
            default -> "twinClassFieldRuleMapEntity [id:" + id + "]" + ", twinClassFieldRuleId=" + twinClassFieldRuleId + "]" + ", twinClassFieldId=" + twinClassFieldId + "]";
        };
    }
}
