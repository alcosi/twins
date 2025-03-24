package org.twins.face.dao.widget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_widget_wt003")
public class FaceWT003Entity implements EasyLoggable{
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "images_twin_class_field_id")
    private UUID imagesTwinClassFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", nullable = false, insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassFields;

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