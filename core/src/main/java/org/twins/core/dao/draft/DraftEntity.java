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

    @Column(name = "draft_status_id")
    @Convert(converter = DraftStatusConverter.class)
    private Status status;

    @Column(name = "draft_status_details")
    private String statusDetails;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;

    @Column(name = "twin_erase_status_count")
    private int twinEraseByStatusCount = 0;

    @Column(name = "twin_erase_irrevocable_count")
    private int twinEraseIrrevocableCount = 0;

    @Column(name = "twin_persist_create_count")
    private int twinPersistCreateCount = 0;

    @Column(name = "twin_persist_update_count")
    private int twinPersistUpdateCount = 0;

    @Column(name = "twin_link_create_count")
    private int twinLinkCreateCount = 0;

    @Column(name = "twin_link_update_count")
    private int twinLinkUpdateCount = 0;

    @Column(name = "twin_link_delete_count")
    private int twinLinkDeleteCount = 0;

    @Column(name = "twin_attachment_create_count")
    private int twinAttachmentCreateCount = 0;

    @Column(name = "twin_attachment_update_count")
    private int twinAttachmentUpdateCount = 0;

    @Column(name = "twin_attachment_delete_count")
    private int twinAttachmentDeleteCount = 0;

    @Column(name = "twin_marker_create_count")
    private int twinMarkerCreateCount = 0;

    @Column(name = "twin_marker_delete_count")
    private int twinMarkerDeleteCount = 0;

    @Column(name = "twin_tag_create_count")
    private int twinTagCreateCount = 0;

    @Column(name = "twin_tag_delete_count")
    private int twinTagDeleteCount = 0;

    @Column(name = "twin_field_simple_create_count")
    private int twinFieldSimpleCreateCount = 0;

    @Column(name = "twin_field_simple_update_count")
    private int twinFieldSimpleUpdateCount = 0;

    @Column(name = "twin_field_simple_delete_count")
    private int twinFieldSimpleDeleteCount = 0;

    @Column(name = "twin_field_user_create_count")
    private int twinFieldUserCreateCount = 0;

    @Column(name = "twin_field_user_update_count")
    private int twinFieldUserUpdateCount = 0;

    @Column(name = "twin_field_user_delete_count")
    private int twinFieldUserDeleteCount = 0;

    @Column(name = "twin_field_data_list_create_count")
    private int twinFieldDataListCreateCount = 0;

    @Column(name = "twin_field_data_list_update_count")
    private int twinFieldDataListUpdateCount = 0;

    @Column(name = "twin_field_data_list_delete_count")
    private int twinFieldDataListDeleteCount = 0;

    public void incrementTwinEraseByStatus() {
        twinEraseByStatusCount++;
    }

    public void incrementTwinEraseIrrevocable() {
        twinEraseIrrevocableCount++;
    }

    public void incrementTwinPersistCreate() {
        twinPersistCreateCount++;
    }

    public void incrementTwinPersistUpdate() {
        twinPersistUpdateCount++;
    }


    public void incrementTwinLinkCreate() {
        twinLinkCreateCount++;
    }

    public void incrementTwinLinkUpdate() {
        twinLinkUpdateCount++;
    }

    public void incrementTwinLinkDelete() {
        twinLinkDeleteCount++;
    }

    public void incrementTwinAttachmentCreate() {
        twinAttachmentCreateCount++;
    }

    public void incrementTwinAttachmentUpdate() {
        twinAttachmentUpdateCount++;
    }

    public void incrementTwinAttachmentDelete() {
        twinAttachmentDeleteCount++;
    }

    public void incrementTwinMarkerCreate() {
        twinMarkerCreateCount++;
    }

    public void incrementTwinMarkerDelete() {
        twinMarkerDeleteCount++;
    }

    public void incrementTwinTagCreate() {
        twinTagCreateCount++;
    }

    public void incrementTwinTagDelete() {
        twinTagDeleteCount++;
    }

    public void incrementTwinFieldSimpleCreate() {
        twinFieldSimpleCreateCount++;
    }

    public void incrementTwinFieldSimpleUpdate() {
        twinFieldSimpleUpdateCount++;
    }

    public void incrementTwinFieldSimpleDelete() {
        twinFieldSimpleDeleteCount++;
    }

    public void incrementTwinFieldUserCreate() {
        twinFieldUserCreateCount++;
    }

    public void incrementTwinFieldUserUpdate() {
        twinFieldUserUpdateCount++;
    }

    public void incrementTwinFieldUserDelete() {
        twinFieldUserDeleteCount++;
    }

    public void incrementTwinFieldDataListCreate() {
        twinFieldDataListCreateCount++;
    }

    public void incrementTwinFieldDataListUpdate() {
        twinFieldDataListUpdateCount++;
    }

    public void incrementTwinFieldDataListDelete() {
        twinFieldDataListDeleteCount++;
    }

    public int getTwinLinkCount() {
        return twinLinkCreateCount + twinLinkUpdateCount + twinLinkDeleteCount;
    }

    public int getTwinAttachmentCount() {
        return twinAttachmentCreateCount + twinAttachmentUpdateCount + twinAttachmentDeleteCount;
    }

    public int getTwinEraseCount() {
        return twinEraseByStatusCount + twinEraseIrrevocableCount;
    }

    public int getTwinPersistCount() {
        return twinPersistCreateCount + twinPersistUpdateCount;
    }

    public int getTwinMarkerCount() {
        return twinMarkerCreateCount + twinMarkerDeleteCount;
    }

    public int getTwinTagCount() {
        return twinTagCreateCount + twinTagDeleteCount;
    }

    public int getTwinFieldSimpleCount() {
        return twinFieldSimpleCreateCount + twinFieldSimpleUpdateCount + twinFieldSimpleDeleteCount;
    }

    public int getTwinFieldDataListCount() {
        return twinFieldDataListCreateCount + twinFieldDataListUpdateCount + twinFieldDataListDeleteCount;
    }

    public int getTwinFieldUserCount() {
        return twinFieldUserCreateCount + twinFieldUserUpdateCount + twinFieldUserDeleteCount;
    }

    public int getAllChangesCount() {
        return twinEraseByStatusCount
                + twinEraseIrrevocableCount
                + twinMarkerCreateCount
                + twinMarkerDeleteCount
                + twinTagCreateCount
                + twinTagDeleteCount
                + twinLinkCreateCount
                + twinLinkUpdateCount
                + twinLinkDeleteCount
                + twinAttachmentCreateCount
                + twinAttachmentUpdateCount
                + twinAttachmentDeleteCount
                + twinFieldSimpleCreateCount
                + twinFieldSimpleUpdateCount
                + twinFieldSimpleDeleteCount
                + twinFieldUserCreateCount
                + twinFieldUserUpdateCount
                + twinFieldUserDeleteCount
                + twinFieldDataListCreateCount
                + twinFieldDataListUpdateCount
                + twinFieldDataListDeleteCount
                + twinPersistCreateCount
                + twinPersistUpdateCount;

    }

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "draft[" + id + "]";
            case NORMAL -> "draft[id:" + id + ", status:" + status + "]";
            default -> "draft[id:" + id + ", status:" + status + ", createdBy:" + createdByUserId + "]";
        };

    }

    public int getTwinFieldCount() {
        return getTwinFieldSimpleCount() + getTwinFieldDataListCount() + getTwinFieldUserCount();
    }

    @Getter
    public enum Status {
        UNDER_CONSTRUCTION("UNDER_CONSTRUCTION"),
        CONSTRUCTION_EXCEPTION("CONSTRUCTION_EXCEPTION"),
        UNCOMMITED("UNCOMMITED"),
        COMMIT_NEED_START("COMMIT_IN_QUEUE"),
        COMMIT_IN_PROGRESS("COMMIT_IN_PROGRESS"),
        COMMIT_EXCEPTION("COMMIT_EXCEPTION"),
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