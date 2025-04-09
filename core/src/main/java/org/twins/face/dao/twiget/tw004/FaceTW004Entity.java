package org.twins.face.dao.twiget.tw004;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceTwidget;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_twidget_tw004")
public class FaceTW004Entity implements EasyLoggable, FaceTwidget {
    @Id
    @Column(name = "face_id")
    private UUID faceId;
    
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", nullable = false, insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW004[" + faceId + "]";
            default:
                return "faceTW004[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}