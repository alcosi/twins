package org.twins.face.dao.bc;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "face_bc001")
public class FaceBC001Entity implements EasyLoggable, FaceVariantEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FaceBC001ItemEntity, UUID> items;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceBC001[" + id + "]";
            default:
                return "faceBC001[id:" + id + ", name:" + face.getName() + "]";
        }
    }
}
