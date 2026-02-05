package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Entity
@DomainSetting
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twinflow_transition_alias")
public class TwinflowTransitionAliasEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "alias")
    private String alias;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Transient
    private Integer inTwinflowTransitionUsagesCount;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinflowTransitionAlias[" + id + "]";
            default:
                return "twinflowTransitionAlias[domain id:" + domainId + ", alias:" + alias + "]";
        }

    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}
