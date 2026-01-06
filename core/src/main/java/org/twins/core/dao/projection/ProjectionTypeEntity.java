package org.twins.core.dao.projection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "projection_type")
public class ProjectionTypeEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "projection_type_group_id")
    private UUID projectionTypeGroupId;

    @Column(name = "membership_twin_class_id")
    private UUID membershipTwinClassId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projection_type_group_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
    private ProjectionTypeGroupEntity projectionTypeGroup;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_twin_class_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity membershipTwinClass;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "projectionType[" + id + "]";
            case NORMAL -> "projectionType[id:" + id + ", key:" + key + ", name:" + name + "]";
            default -> "projectionType[id:" + id + ", key:" + key + ", name:" + name + ", projectionTypeGroupId:" + projectionTypeGroupId + ", membershipTwinClassId" + membershipTwinClassId + "]";
        };
    }
}
