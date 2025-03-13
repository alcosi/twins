package org.twins.core.dao.face.navbar;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.i18n.dao.I18nEntity;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.resource.ResourceEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_navbar_nb001_menu_items")
public class FaceNB001MenuItemEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Basic
    @Column(name = "face_id")
    private UUID faceId;

    @NotNull
    @Column(name = "key")
    private String key;

    @Basic
    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "icon_dark_resource_id")
    private UUID iconDarkResourceId;

    @Column(name = "icon_light_resource_id")
    private UUID iconLightResourceId;

    @Column(name = "face_navbar_nb001_status_id")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "target_page_face_id")
    private UUID targetPageFaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_dark_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconDarkResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_light_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconLightResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_page_face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity targetPageFace;

    public enum Status {
        ACTIVE,
        DISABLED,
        HIDDEN
    }
}