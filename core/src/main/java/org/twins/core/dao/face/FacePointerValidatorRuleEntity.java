package org.twins.core.dao.face;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "face_pointer_validator_rule")
public class FacePointerValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_pointer")
    private UUID faceTwinPointer;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer", insertable = false, updatable = false)
    private FacePointerEntity facePointer;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", referencedColumnName = "twin_validator_set_id", insertable = false, updatable = false)
    private Set<TwinValidatorEntity> twinValidators;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTwinPointerValidatorRule[" + id + "]";
            default:
                return "faceTwinPointerValidatorRule[id:" + id + ", faceTwinPointer:" + faceTwinPointer + "]";
        }
    }
}
