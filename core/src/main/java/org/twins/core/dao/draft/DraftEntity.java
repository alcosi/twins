package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.draft.DraftCounters;

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
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "draft_status_id")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "draft_status_details")
    private String statusDetails;

    @Column(name = "auto_commit")
    private boolean autoCommit;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
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

    @Getter
    public enum Status {
        UNDER_CONSTRUCTION,
        CONSTRUCTION_EXCEPTION,
        ERASE_SCOPE_COLLECT_PLANNED,
        ERASE_SCOPE_COLLECT_NEED_START,
        ERASE_SCOPE_COLLECT_IN_PROGRESS,
        ERASE_SCOPE_COLLECT_EXCEPTION,
        NORMALIZE_EXCEPTION,
        CHECK_CONFLICTS_EXCEPTION,
        UNCOMMITED,
        COMMIT_NEED_START,
        COMMIT_IN_PROGRESS,
        COMMIT_EXCEPTION,
        LOCKED,
        OUT_OF_DATE,
        COMMITED;
    }
}