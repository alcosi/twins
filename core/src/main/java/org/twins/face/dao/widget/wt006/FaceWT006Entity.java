package org.twins.face.dao.widget.wt006;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldSearchEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt006")
public class FaceWT006Entity implements FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "twin_class_field_search_id", insertable = false, updatable = false)
    private UUID twinClassFieldSearchId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "editable_field_filter_featurer_id", insertable = false, updatable = false)
    private Integer fieldFilterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "editable_field_filter_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldFilterParams;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRuleSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_twin_pointer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity targetTwinPointerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity targetTwinPointer;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_search_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldSearchEntity twinClassFieldSearchSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldSearchEntity twinClassFieldSearch;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editable_field_filter_featurer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity fieldFilterFeaturerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity fieldFilterFeaturer;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity faceSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT006[" + faceId + "]";
            default:
                return "faceWT006[id:" + faceId + (face != null ? ", componentId:" + face.getFaceComponentId() : "") + "]";
        }
    }
}
