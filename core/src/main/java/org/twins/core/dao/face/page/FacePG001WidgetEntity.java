package org.twins.core.dao.face.page;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.ColumnDefault;
import org.twins.core.dao.face.FaceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_page_pg001_widget")
public class FacePG001WidgetEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ColumnDefault("0")
    @Column(name = "widget_order", nullable = false)
    private Integer widgetOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity widgetFace;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001Widget[" + id + "]";
            default:
                return "facePG001Widget[id:" + id + ", faceId:" + faceId + "]";
        }
    }
}