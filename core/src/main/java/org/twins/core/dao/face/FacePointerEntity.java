package org.twins.core.dao.face;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "face_pointer")
public class FacePointerEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "pointer_featurer_id")
    private Integer pointerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "pointer_params")
    private HashMap<String, String> pointerParams;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", insertable = false, updatable = false)
    private FaceEntity face;

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
