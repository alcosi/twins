package org.twins.face.dao.twidget.tw004;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceTwidget;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_tw004")
public class FaceTW004Entity implements EasyLoggable, FaceTwidget {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Column(name = "field_finder_featurer_id", insertable = false, updatable = false)
    private Integer fieldFinderFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_finder_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldFinderParams;

    @Column(name = "editable_field_filter_featurer_id", insertable = false, updatable = false)
    private Integer fieldFilterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "editable_field_filter_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldFilterParams;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW004[" + faceId + "]";
            default:
                return "faceTW004[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}