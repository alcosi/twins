package org.twins.face.dao.widget.wt002;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_widget_wt002_button")
public class FaceWT002ButtonEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id", nullable = false)
    private UUID faceId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "extends_depth")
    private Integer extendsDepth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;
}
