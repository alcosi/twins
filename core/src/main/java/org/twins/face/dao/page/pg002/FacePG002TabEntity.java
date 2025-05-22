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
import org.twins.core.dao.resource.ResourceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_page_pg002_tab")
public class FacePG002TabEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity titleI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;

    @Column(name = "`order`")
    private short order;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FacePG002WidgetEntity, UUID> widgets;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG002Tab[" + faceId + "]";
            default:
                return "facePG002Tab[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}