package org.twins.core.dao.projection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "projection_exclusion")
public class ProjectionExclusionEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Override
    public String easyLog(Level level) {
        return "projectionExclusion[id:" + id + "]";
    }
}
