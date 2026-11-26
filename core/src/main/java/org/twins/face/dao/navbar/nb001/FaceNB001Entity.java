package org.twins.face.dao.navbar.nb001;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "face_nb001")
public class FaceNB001Entity implements EasyLoggable, Identifiable<UUID> {
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

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity faceSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_area_icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity adminAreaIconResourceSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_area_icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity userAreaIconResourceSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity adminAreaIconResource;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity userAreaIconResource;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FaceNB001MenuItemEntity, UUID> menuItems;

    @Override
    public UUID getId() {
        return faceId;
    }

    @Override
    public FaceNB001Entity setId(UUID id) {
        this.faceId = id;
        return this;
    }

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceNB001[" + faceId + "]";
            default:
                return "faceNB001[id:" + faceId + (face != null ? ", componentId:" + face.getFaceComponentId() : "") + "]";
        }
    }
}
