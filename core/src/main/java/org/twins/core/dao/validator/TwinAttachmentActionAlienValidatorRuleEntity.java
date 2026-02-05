package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;
import org.twins.core.enums.attachment.TwinAttachmentAction;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@DomainSetting
@Table(name = "twin_attachment_action_alien_validator_rule")
public class TwinAttachmentActionAlienValidatorRuleEntity implements ContainsTwinValidatorSet, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction twinAttachmentAction;

    @Column(name = "`order`")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinAttachmentActionAlienValidatorRule[" + id + "]";
            case NORMAL -> "twinAttachmentActionAlienValidatorRule[id:" + id + ", twinClassId:" + twinClassId + "]";
            default -> "twinAttachmentActionAlienValidatorRule[id:" + id + ", twinClassId:" + twinClassId
                    + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
