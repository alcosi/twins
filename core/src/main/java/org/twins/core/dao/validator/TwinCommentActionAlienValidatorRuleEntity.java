package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.enums.comment.TwinCommentAction;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_comment_action_alien_validator_rule")
@Accessors(chain = true)
public class TwinCommentActionAlienValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction twinCommentAction;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinValidatorEntity, UUID> twinValidatorKit;

    @Transient
    private TwinValidatorSetEntity twinValidatorSet;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinCommentActionAlienValidatorRule[" + id + "]";
            case NORMAL -> "twinCommentActionAlienValidatorRule[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinCommentActionAlienValidatorRule[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }

}
