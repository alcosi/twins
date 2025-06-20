package org.twins.face.dao.page.pg002;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_pg002")
public class FacePG002Entity implements EasyLoggable {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "face_pg002_layout_id")
    @Enumerated(EnumType.STRING)
    private Layout layout;

    @Column(name = "style_classes")
    private String styleClasses;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity titleI18n;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FacePG002TabEntity, UUID> tabs;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG002[" + faceId + "]";
            default:
                return "facePG002[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }

    public enum Layout {
        TOP, BOTTOM, LEFT, RIGHT
    }
}