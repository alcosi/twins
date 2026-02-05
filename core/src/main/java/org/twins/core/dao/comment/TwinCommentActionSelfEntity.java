package org.twins.core.dao.comment;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;
import org.twins.core.enums.comment.TwinCommentAction;

import java.util.UUID;

@Entity
@Data
@DomainSetting
@Table(name = "twin_comment_action_self")
public class TwinCommentActionSelfEntity {
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

    @Column(name = "restrict_twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction restrictTwinCommentAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}
