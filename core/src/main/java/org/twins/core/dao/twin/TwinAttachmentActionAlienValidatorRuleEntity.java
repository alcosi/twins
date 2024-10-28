
package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_attachment_action_alien_validator_rule")
public class TwinAttachmentActionAlienValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction twinAttachmentAction;

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @OneToMany
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private List<TwinValidatorEntity> twinValidators;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinCommentActionAlienValidatorRule[" + id + "]";
            case NORMAL -> "twinCommentActionAlienValidatorRule[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinCommentActionAlienValidatorRule[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
