package org.twins.face.dao.navbar.nb001;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinEntity;

import java.util.UUID;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "face_nb001_menu_item")
public class FaceNB001MenuItemEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "key")
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "face_nb001_status_id")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Deprecated
    @Column(name = "target_page_face_id")
    private UUID targetPageFaceId;

    @Column(name = "target_twin_id")
    private UUID targetTwinId;

    @Column(name = "counter_twin_search_id")
    private UUID counterTwinSearchId;

    @Column(name = "parent_face_nb001_menu_item_id")
    private UUID parentFaceMenuItemId;

    @Column(name = "`order`")
    private Integer order;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity faceSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResourceSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_page_face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity targetPageFaceSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_twin_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity targetTwinSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity permissionSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResource;

    @Deprecated
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity targetPageFace;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity permission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FaceNB001MenuItemEntity, UUID> childs;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceNB001Entity faceNB001;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceNB001MenuItemEntity parentFaceMenuItem;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Long counter;

    @Override
    public String easyLog(Level level) {
        return "faceNB001MenuItem[id:" + id + "]";
    }

    public enum Status {
        ACTIVE,
        DISABLED,
        HIDDEN
    }
}
