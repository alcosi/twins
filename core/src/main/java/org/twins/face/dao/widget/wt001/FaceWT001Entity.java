package org.twins.face.dao.widget.wt001;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Data
@Entity
@Table(name = "face_wt001")
public class FaceWT001Entity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_search_id")
    private UUID searchId;

    @Column(name = "search_target_twin_pointer_id")
    private UUID searchTargetTwinPointerId;

    @Column(name = "modal_face_id")
    private UUID modalFaceId;

    @Column(name = "show_create_button", nullable = false)
    private boolean showCreateButton;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modal_face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity modalFace;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceWT001ColumnEntity, UUID> columns;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        switch (level) {
            case SHORT:
                return "faceWT001[" + faceId + "]";
            default:
                return "faceWT001[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}