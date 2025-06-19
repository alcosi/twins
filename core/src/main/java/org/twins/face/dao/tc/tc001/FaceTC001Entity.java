package org.twins.face.dao.tc.tc001;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_tc001")
public class FaceTC001Entity {
    @Id
    @Column(name = "face_id", nullable = false)
    private UUID faceId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "header_i18n_id")
    private UUID headerI18nId;

    @Column(name = "header_icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "extends_depth")
    private Integer extendsDepth;

    @Column(name = "head_pointer_featurer_id")
    private Integer headPointerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "head_pointer_params", columnDefinition = "hstore")
    private HashMap<String, String> headPointerParams;

    @Column(name = "field_finder_featurer_id")
    private Integer fieldFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldFinderParams;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_i18n_id", insertable = false, updatable = false)
    private I18nEntity headerI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldEntity, UUID> fields;
}
