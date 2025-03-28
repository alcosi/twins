package org.twins.face.dao.page.pg001;

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
@Table(name = "face_page_pg001")
public class FacePG001Entity implements EasyLoggable {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "face_page_pg001_layout_id")
    @Enumerated(EnumType.STRING)
    private Layout layout;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity titleI18n;
    
    //todo add more properties

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FacePG001WidgetEntity, UUID> widgets;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001[" + faceId + "]";
            default:
                return "facePG001[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }

    public enum Layout {
        ONE_COLUMN, TWO_COLUMNS, THREE_COLUMNS
    }
}