package org.twins.face.dao.twidget.tw005;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceTwidget;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_twidget_tw005")
public class FaceTW005Entity implements EasyLoggable, FaceTwidget {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "align_vertical", nullable = false)
    private boolean alignVertical;

    @Column(name = "glue", nullable = false)
    private boolean glue;

    @Column(name = "style_classes")
    private String styleClasses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceTW005ButtonEntity, UUID> buttons;


    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW005[" + faceId + "]";
            default:
                return "faceTW005[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}