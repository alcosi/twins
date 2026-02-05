package org.twins.core.dao.trigger;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Entity that represents async task created for twin trigger processing.
 */
@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_trigger_task")
public class TwinTriggerTaskEntity implements EasyLoggable {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_trigger_id")
    private UUID twinTriggerId;

    @Column(name = "previous_twin_status_id")
    private UUID previousTwinStatusId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_trigger_task_status_id")
    private TwinTriggerTaskStatus statusId;

    @Column(name = "twin_trigger_status_details")
    private String statusDetails;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "done_at")
    private Timestamp doneAt;

    /* Relations */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_trigger_id", insertable = false, updatable = false, nullable = false)
    private TwinTriggerEntity twinTrigger;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "previous_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity previousTwinStatus;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL ->
                    "twinTriggerTask[id:" + id + ", twinId:" + twinId + ", triggerId:" + twinTriggerId + "]";
            case DETAILED ->
                    "twinTriggerTask[id:" + id + ", twinId:" + twinId + ", triggerId:" + twinTriggerId + ", userId:" + createdByUserId + ", businessAccountId:" + businessAccountId + "]";
            default -> "twinTriggerTask[id:" + id + "]";
        };
    }
}
