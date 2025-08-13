package org.twins.face.dao.page.pg002;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.UUID;

@Data
@Entity
@Table(name = "face_pg002_widget")
public class FacePG002WidgetEntity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_pg002_tab_id")
    private UUID facePagePG002TabId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity widgetFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG002Widget[" + id + "]";
            default:
                return "facePG002Widget[id:" + id + ", facePagePG002TabId:" + facePagePG002TabId + "]";
        }
    }
}
