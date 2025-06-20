package org.twins.face.dao.page.pg002;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_pg002_widget")
public class FacePG002WidgetEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_pg002_tab_id")
    private UUID facePagePG002TabId;

    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity widgetFace;

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