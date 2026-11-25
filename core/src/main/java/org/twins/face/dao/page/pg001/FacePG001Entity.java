package org.twins.face.dao.page.pg001;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_pg001")
public class FacePG001Entity implements FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "style_classes")
    private String styleClasses;

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
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity faceSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FacePG001WidgetEntity, UUID> widgets;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001[" + faceId + "]";
            default:
                return "facePG001[id:" + faceId + (face != null ? ", componentId:" + face.getFaceComponentId() : "") + "]";
        }
    }

}
