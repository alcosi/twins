package org.twins.core.dao.face;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.featurer.FeaturerEntity;

import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "face_twin_pointer")
public class FaceTwinPointerEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "pointer_featurer_id")
    private Integer pointerFeaturerId;

    @Type(value = org.hibernate.type.MapType.class)
    @Column(name = "pointer_params")
    private Map<String, String> pointerParams;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pointer_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity pointerFeaturer;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTwinPointer[" + id + "]";
            default:
                return "faceTwinPointer[id:" + id + ", faceId:" + faceId + "]";
        }
    }
}
