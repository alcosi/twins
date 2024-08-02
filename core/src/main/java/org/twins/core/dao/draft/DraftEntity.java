package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.Arrays;
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

    @Column(name = "twin_create_count")
    private Integer twinCreateCount;

    @Column(name = "twin_update_count")
    private Integer twinUpdateCount;

    @Column(name = "twin_erase_count")
    private Integer twinEraseCount;

    @Column(name = "draft_status_id")
    @Convert(converter = DraftStatusConverter.class)
    private Status status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;

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
        UNDER_CONSTRUCTION("UNDER_CONSTRUCTION"),
        UNCOMMITED("UNCOMMITED"),
        LOCKED("LOCKED"),
        OUT_OF_DATE("OUT_OF_DATE"),
        COMMITED("COMMITED");

        private final String id;

        Status(String id) {
            this.id = id;
        }

        public static Status valueOd(String type) {
            return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElseThrow();
        }

    }
}