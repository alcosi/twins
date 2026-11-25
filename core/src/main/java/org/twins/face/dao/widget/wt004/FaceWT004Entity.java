package org.twins.face.dao.widget.wt004;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt004")
public class FaceWT004Entity implements FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "images_twin_class_field_id")
    private UUID imagesTwinClassFieldId;

    @Column(name = "uploadable")
    private Boolean uploadable;

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
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity faceSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "images_twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity imagesTwinClassFieldSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity imagesTwinClassField;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinAttachmentRestrictionEntity twinAttachmentRestriction;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT004[" + faceId + "]";
            default:
                return "faceWT004[id:" + faceId + (face != null ? ", componentId:" + face.getFaceComponentId() : "") + "]";
        }
    }
}
