package org.twins.face.dao.navbar;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_navbar_nb001")
public class FaceNB001Entity implements EasyLoggable {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "skin")
    private String skin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceNB001[" + faceId + "]";
            default:
                return "faceNB001[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceNB001MenuItemEntity, UUID> menuItems;
}