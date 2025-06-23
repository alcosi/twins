package org.twins.face.dao.page.pg001;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;
import org.twins.core.dao.face.FaceVariant;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_pg001")
public class FacePG001Entity implements EasyLoggable, FaceVariant {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "face_pointer_validator_rule_id")
    private UUID facePointerValidatorRuleId;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "style_classes")
    private String styleClasses;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer_validator_rule_id", insertable = false, updatable = false)
    private FacePointerValidatorRuleEntity facePointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity titleI18n;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FacePG001WidgetEntity, UUID> widgets;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001[" + faceId + "]";
            default:
                return "facePG001[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }

}