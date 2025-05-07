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
@Table(name = "face_widget_wt002")
public class FaceWT002Entity {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "key", nullable = false)
    private String key;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "style_attributes", columnDefinition = "hstore")
    private HashMap<String, String> styleAttributes;
}
