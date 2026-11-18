package org.twins.face.dao.widget.wt009;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_wt009")
public class FaceWT009Entity implements EasyLoggable, FacePointedEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "ui_type")
    private String uiType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FaceWT009ActionEntity, UUID> actions;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT009[" + faceId + "]";
            default:
                return "faceWT009[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}
