package org.twins.face.dao.navbar.nb001;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "face_navbar_nb001")
public class FaceNB001Entity implements EasyLoggable {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "admin_area_label_i18n_id")
    private UUID adminAreaLabelI18nId;

    @Column(name = "admin_area_icon_resource_id")
    private UUID adminAreaIconResourceId;

    @Column(name = "user_area_label_i18n_id")
    private UUID userAreaLabelI18nId;

    @Column(name = "user_area_icon_resource_id")
    private UUID userAreaIconResourceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_area_label_i18n_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity adminAreaLabelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_area_icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity adminAreaIconResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_area_label_i18n_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity userAreaLabelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_area_icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity userAreaIconResource;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceNB001[" + faceId + "]";
            default:
                return "faceNB001[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceNB001MenuItemEntity, UUID> menuItems;
}