package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.draft.DraftCounters;
import org.twins.core.enums.draft.DraftStatus;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft")
public class DraftEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "draft_status_id")
    @Enumerated(EnumType.STRING)
    private DraftStatus status;

    @Column(name = "draft_status_details")
    private String statusDetails;

    @Column(name = "auto_commit")
    private boolean autoCommit;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    private DraftCounters counters = new DraftCounters();

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "draft[" + id + "]";
            case NORMAL -> "draft[id:" + id + ", status:" + status + "]";
            default -> "draft[id:" + id + ", status:" + status + ", createdBy:" + createdByUserId + "]";
        };

    }

}
