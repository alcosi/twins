package org.twins.core.dao.face;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face")
public class FaceEntity {
    @Id
    private UUID id;

    @Column(name = "face_component_id")
    private String faceComponentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "face_component_id", nullable = false)
    private FaceComponentTypeEntity faceComponent;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

}