package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.comment.TwinCommentAction;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "twin_comment_action_alien_validator_rule")
public class TwinCommentActionAlienValidatorRuleEntity implements Validator, EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction twinCommentAction;

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", insertable = false, updatable = false)
    private List<TwinValidatorEntity> twinValidators;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinCommentActionAlienValidator[" + id + "]";
            case NORMAL -> "twinCommentActionAlienValidator[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinCommentActionAlienValidator[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }

}
