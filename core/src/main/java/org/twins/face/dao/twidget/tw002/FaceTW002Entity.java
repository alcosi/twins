package org.twins.face.dao.twidget.tw002;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_tw002")
public class FaceTW002Entity implements EasyLoggable, FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "face_pointer_validator_rule_id")
    private UUID facePointerValidatorRuleId;

    @Column(name = "target_twin_face_pointer_id")
    private UUID targetTwinFacePointerId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "i18n_twin_class_field_id")
    private UUID i18nTwinClassFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer_validator_rule_id", insertable = false, updatable = false)
    private FacePointerValidatorRuleEntity facePointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_twin_class_field_id", nullable = false, insertable = false, updatable = false)
    private TwinClassFieldEntity i18nTwinClassField;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW002[" + faceId + "]";
            default:
                return "faceTW002[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceTW002AccordionItemEntity, UUID> accordionItems;
}
