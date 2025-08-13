package org.twins.face.dao.widget.wt003;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Data
@Entity
@Table(name = "face_wt003")
public class FaceWT003Entity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private FaceWT003Level level;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "message_i18n_id")
    private UUID messageI18nId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "substitution_from_twin_pointer_id")
    private UUID substitutionFromTwinPointerId;

    @Column(name = "title_substitution_twin_class_field_id")
    private UUID titleSubstitutionTwinClassFieldId;

    @Column(name = "message_substitution_twin_class_field_id")
    private UUID messageSubstitutionTwinClassFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitution_from_twin_pointer_id", insertable = false, updatable = false)
    private TwinPointerEntity substitutionFromTwinPointer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_substitution_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity substitutionTitleField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_substitution_twin_class_field_id", insertable = false, updatable = false)
    private TwinClassFieldEntity substitutionMessageField;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT003[" + faceId + "]";
            default:
                return "faceWT003[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}
