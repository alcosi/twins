package org.twins.core.dao.face.page;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_page_pg001")
public class FacePG001Entity implements EasyLoggable {
    @Id
    @Column(name = "face_id")
    private UUID faceId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;
    
    //todo add more properties

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FacePG001WidgetEntity, UUID> widgets;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001[" + faceId + "]";
            default:
                return "facePG001[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}