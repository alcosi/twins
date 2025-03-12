package org.twins.core.dao.face;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face")
public class FaceEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "face_component_id")
    private String faceComponentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "face_component_id", nullable = false)
    private FaceComponentTypeEntity faceComponent;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "face[" + id + "]";
            default:
                return "face[id:" + id + ", componentId:" + faceComponentId + "]";
        }
    }
}