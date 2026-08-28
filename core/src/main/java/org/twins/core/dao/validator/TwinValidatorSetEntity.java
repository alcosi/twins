package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_validator_set")
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorSetEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "invert")
    private Boolean invert;

    // Trigger-maintained usage-counter column (created in V1.4.349.01, maintained by AFTER triggers).
    // insertable=false/updatable=false keeps Hibernate out of the write path (otherwise INSERT sends
    // NULL into a NOT NULL DEFAULT 0 column, and UPDATE clobbers the trigger); @Generated makes
    // Hibernate re-read the row after INSERT/UPDATE so the in-memory value stays correct.
    // Same pattern as TwinFactoryConditionSetEntity.usageCountPipeline.
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count", insertable = false, updatable = false)
    private Integer usageCount;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinValidatorSetEntity[" + id + "]";
            case NORMAL -> "twinValidatorSetEntity[id:" + id + ", domainId:" + domainId + "]";
            default -> "twinValidatorSetEntity[id:" + id + ", domainId:" + domainId + ", name:" + name + ", usageCount:" + usageCount + "]";
        };
    }

}
