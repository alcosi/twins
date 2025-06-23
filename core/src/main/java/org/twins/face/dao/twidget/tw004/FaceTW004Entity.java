package org.twins.face.dao.twidget.tw004;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_tw004")
public class FaceTW004Entity implements EasyLoggable, FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "face_pointer_validator_rule_id")
    private UUID facePointerValidatorRuleId;

    @Column(name = "target_twin_face_pointer_id")
    private UUID targetTwinFacePointerId;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer_validator_rule_id", insertable = false, updatable = false)
    private FacePointerValidatorRuleEntity facePointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

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
