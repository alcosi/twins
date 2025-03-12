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
@Table(name = "face_navbar_nb001")
public class FaceNB001Entity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Basic
    @Column(name = "face_id")
    private UUID faceId;

    @NotNull
    @Column(name = "key", nullable = false, length = Integer.MAX_VALUE)
    private String key;

    @Basic
    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "description_i18n_id", length = Integer.MAX_VALUE)
    private String descriptionI18nId;

    @Column(name = "face_navbar_nb001_status_id")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "face_id", nullable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", nullable = false)
    private I18nEntity descriptionI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_dark_resource_id")
    private ResourceEntity iconDarkResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_light_resource_id")
    private ResourceEntity iconLightResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_face_id", nullable = false)
    private FaceEntity pageFace;

    public enum Status {
        ACTIVE,
        DISABLED,
        HIDDEN
    }
}