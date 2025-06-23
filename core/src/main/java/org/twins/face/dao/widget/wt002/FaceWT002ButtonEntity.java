package org.twins.face.dao.widget.wt002;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceTwinPointerValidatorRuleEntity;
import org.twins.core.dao.face.FaceVariant;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_wt002_button")
public class FaceWT002ButtonEntity implements EasyLoggable, FaceVariant {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_wt002_id", nullable = false)
    private UUID faceWT002Id;

    @Column(name = "face_twin_pointer_validator_rule_id")
    private UUID faceTwinPointerValidatorRuleId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "modal_face_id")
    private UUID modalFaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_twin_pointer_validator_rule_id", insertable = false, updatable = false)
    private FaceTwinPointerValidatorRuleEntity faceTwinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modal_face_id", insertable = false, updatable = false)
    private FaceEntity modalFace;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT002Button[" + id + "]";
            default:
                return "faceWT002Button[id:" + id + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}
