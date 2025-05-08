package org.twins.face.dao.widget.wt002;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;

import java.util.Set;
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
    @Column(name = "style_classes")
    private Set<String> styleClasses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceWT002ButtonEntity, UUID> buttons;
}
