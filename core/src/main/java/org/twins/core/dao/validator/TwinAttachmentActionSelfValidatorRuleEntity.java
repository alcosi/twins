
package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.BatchSize;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;
import org.twins.core.enums.attachment.TwinAttachmentAction;

import java.util.Set;
import java.util.UUID;

@Data
@Entity
@DomainSetting
@Table(name = "twin_attachment_action_self_validator_rule")
public class TwinAttachmentActionSelfValidatorRuleEntity implements EasyLoggable {
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

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "active")
    private boolean isActive;

    @Column(name = "restrict_twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction restrictTwinAttachmentAction;

    @Column(name = "twin_validator_set_id")
    private UUID twinValidatorSetId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_validator_set_id", referencedColumnName = "twin_validator_set_id", insertable = false, updatable = false)
    @BatchSize(size = 20)
    private Set<TwinValidatorEntity> twinValidators;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinAttachmentActionSelfValidatorRule[" + id + "]";
            case NORMAL -> "twinAttachmentActionSelfValidatorRule[id:" + id + ", twinClassId:" + twinClassId + "]";
            default -> "twinAttachmentActionSelfValidatorRule[id:" + id + ", twinClassId:" + twinClassId
                    + ", twinValidatorSetId:" + twinValidatorSetId + "]";
        };
    }
}
