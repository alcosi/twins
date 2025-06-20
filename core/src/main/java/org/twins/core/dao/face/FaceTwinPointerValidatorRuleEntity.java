package org.twins.core.dao.face;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "face_twin_pointer_validator_rule")
public class FaceTwinPointerValidatorRuleEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_twin_pointer")
    private UUID faceTwinPointer;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_twin_pointer", insertable = false, updatable = false)
    private FaceTwinPointerEntity faceTwinPointerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private TwinValidatorSetEntity twinValidatorSet;

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
