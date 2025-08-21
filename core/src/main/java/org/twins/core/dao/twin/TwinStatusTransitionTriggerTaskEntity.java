package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Entity that represents async task created for twin status transition trigger processing.
 * Fields are aligned with DDL in comment below.
 */
@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_status_transition_trigger_task")
@FieldNameConstants
public class TwinStatusTransitionTriggerTaskEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "twin_status_transition_trigger_id")
    private UUID twinStatusTransitionTriggerId;

    @Column(name = "src_twin_status_id")
    private UUID srcTwinStatusId;

    @Column(name = "dst_twin_status_id")
    private UUID dstTwinStatusId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_status_transition_trigger_task_status_id")
    private TwinStatusTransitionTriggerStatus statusId;

    @Column(name = "twin_status_transition_trigger_task_status_details")
    private String statusDetails;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "done_at")
    private Timestamp doneAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_status_transition_trigger_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusTransitionTriggerEntity twinStatusTransitionTrigger;

    @ManyToOne
    @JoinColumn(name = "src_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity srcTwinStatus;

    @ManyToOne
    @JoinColumn(name = "dst_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity dstTwinStatus;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL ->
                    "twinStatusTransitionTriggerTask[id:" + id + ", twinId:" + twinId + ", triggerId:" + twinStatusTransitionTriggerId + "]";
            case DETAILED ->
                    "twinStatusTransitionTriggerTask[id:" + id + ", twinId:" + twinId + ", triggerId:" + twinStatusTransitionTriggerId + ", userId:" + createdByUserId + ", businessAccountId:" + businessAccountId + "]";
            default -> "twinStatusTransitionTriggerTask[id:" + id + "]";
        };
    }
}