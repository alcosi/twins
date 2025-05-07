package org.twins.face.dao.widget.wt002;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.util.HashMap;
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

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "style_attributes", columnDefinition = "hstore")
    private HashMap<String, String> styleAttributes;

    @Column(name = "extends_hierarchy_twin_class_id")
    private UUID extendsHierarchyTwinClassId;

    @Column(name = "extends_hierarchy_depth")
    private int extendsHierarchyDepth;
}
