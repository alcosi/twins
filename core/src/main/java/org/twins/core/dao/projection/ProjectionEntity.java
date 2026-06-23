package org.twins.core.dao.projection;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "projection")
public class ProjectionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "src_twin_pointer_id")
    private UUID srcTwinPointerId;

    @Column(name = "src_twin_class_field_id")
    private UUID srcTwinClassFieldId;

    @Column(name = "dst_twin_class_id")
    private UUID dstTwinClassId;

    @Column(name = "dst_twin_class_field_id")
    private UUID dstTwinClassFieldId;

    @Column(name = "projection_type_id")
    private UUID projectionTypeId;

    @Column(name = "field_projector_featurer_id")
    private Integer fieldProjectorFeaturerId;

    @Column(name = "active")
    private Boolean active;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_projector_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldProjectorParams;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_pointer_id", insertable = false, updatable = false)
    private TwinPointerEntity srcTwinPointerSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity srcTwinClassFieldSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity dstTwinClassFieldSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity dstTwinClassSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projection_type_id", insertable = false, updatable = false)
    private ProjectionTypeEntity ProjectionTypeSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity srcTwinPointer;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity srcTwinClassField;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity dstTwinClassField;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity dstTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ProjectionTypeEntity ProjectionType;

    @Override
    public String easyLog(Level level) {
        return "projection[id:" + id + "]";
    }
}
