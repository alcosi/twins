package org.twins.face.dao.widget.wt003;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;
import org.twins.core.dao.face.FaceVariant;
import org.twins.core.dao.resource.ResourceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_wt003")
public class FaceWT003Entity implements EasyLoggable, FaceVariant {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "face_pointer_validator_rule_id")
    private UUID facePointerValidatorRuleId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer_validator_rule_id", insertable = false, updatable = false)
    private FacePointerValidatorRuleEntity facePointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;

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
