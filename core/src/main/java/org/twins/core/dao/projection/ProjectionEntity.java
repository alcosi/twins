package org.twins.core.dao.projection;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
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
    @GeneratedValue(generator = "uuid")
    private UUID id;

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

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_projector_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldProjectorParams;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_pointer_id", insertable = false, updatable = false)
    private TwinPointerEntity srcTwinPointer;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity srcTwinClassField;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity dstTwinClassField;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity dstTwinClass;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projection_type_id", insertable = false, updatable = false)
    private ProjectionTypeEntity ProjectionType;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity fieldProjectorFeaturer;

    @Override
    public String easyLog(Level level) {
        return "projection[id:" + id + "]";
    }
}
