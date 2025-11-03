package org.twins.core.dao.twinclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
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

    @Column(name = "twin_class_field_rule_id")
    private UUID twinClassFieldRuleId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    /**
     * @param level
     * @return
     */
    @Override
    public String easyLog(Level level) {
        return "TwinClassFieldRuleMapEntity [id=" + id + "]";
    }
}
