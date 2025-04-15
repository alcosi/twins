package org.twins.face.dao.page.pg002;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_page_pg002_widget")
public class FacePG002WidgetEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_page_pg002_tab_id")
    private UUID facePagePG002TabId;

    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_page_pg002_tab_id", nullable = false, insertable = false, updatable = false)
    private FacePG002TabEntity facePagePG002Tab;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "layout_container_item_attributes", columnDefinition = "hstore")
    private HashMap<String, String> containerItemAttributes;

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