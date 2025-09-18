
package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggable;
import org.twins.core.domain.enum_.attachment.TwinAttachmentAction;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "twin_attachment_action_self_validator_rule")
public class TwinAttachmentActionSelfValidatorRuleEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "restrict_twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction restrictTwinAttachmentAction;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    //TODO think over @ManyToMany https://alcosi.atlassian.net/browse/TWINS-220
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_set_id", referencedColumnName = "twin_validator_set_id", insertable = false, updatable = false)
    private Set<TwinValidatorEntity> twinValidators;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinAttachmentActionSelfValidatorRule[" + id + "]";
            case NORMAL -> "twinAttachmentActionSelfValidatorRule[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinAttachmentActionSelfValidatorRule[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
