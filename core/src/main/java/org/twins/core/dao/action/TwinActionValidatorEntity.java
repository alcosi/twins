package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twin.TwinValidatorEntity;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_action_validator")
public class TwinActionValidatorEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAction twinAction;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private List<TwinValidatorEntity> twinValidators;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinActionValidator[" + id + "]";
            case NORMAL -> "twinActionValidator[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinActionValidator[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
