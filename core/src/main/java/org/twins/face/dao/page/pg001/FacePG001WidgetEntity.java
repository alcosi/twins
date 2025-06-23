package org.twins.face.dao.page.pg001;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;
import org.twins.core.dao.face.FaceVariant;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_pg001_widget")
public class FacePG001WidgetEntity implements EasyLoggable, FaceVariant {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_pg001_id")
    private UUID facePG001Id;

    @Column(name = "face_pointer_validator_rule_id")
    private UUID facePointerValidatorRuleId;

    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity widgetFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer_validator_rule_id", insertable = false, updatable = false)
    private FacePointerValidatorRuleEntity facePointerValidatorRule;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001Widget[" + id + "]";
            default:
                return "facePG001Widget[id:" + id + ", faceId:" + facePG001Id + "]";
        }
    }
}