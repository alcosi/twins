package org.twins.face.dao.widget.wt003;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.resource.ResourceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_widget_wt003")
public class FaceWT003Entity {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private FaceWT003Level level;

    @Column(name = "title_i18n_id")
    private UUID titleI18nId;

    @Column(name = "message_i18n_id")
    private UUID messageI18nId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;
}
