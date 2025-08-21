package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Entity that represents async task created for twinflow transition trigger processing.
 * Fields are aligned with DDL in comment above.
 */
@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition_trigger_task")
@FieldNameConstants
public class TwinflowTransitionTriggerTaskEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "twinflow_transition_trigger_id")
    private UUID twinflowTransitionTriggerId;

    @Column(name = "src_twin_status_id")
    private UUID srcTwinStatusId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twinflow_transition_trigger_task_status_id")
    private TwinflowTransitionTriggerStatus statusId;

    @Column(name = "twinflow_transition_trigger_task_status_details")
    private String statusDetails;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "done_at")
    private Timestamp doneAt;

    /* Relations */
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twinflow_transition_trigger_id", insertable = false, updatable = false, nullable = false)
    private TwinflowTransitionTriggerEntity twinflowTransitionTrigger;

    @ManyToOne
    @JoinColumn(name = "src_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity srcTwinStatus;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL ->
                    "twinflowTransitionTriggerTask[id:" + id + ", twinId:" + twinId + ", triggerId:" + twinflowTransitionTriggerId + "]";
            case DETAILED ->
                    "twinflowTransitionTriggerTask[id:" + id + ", twinId:" + twinId + ", triggerId:" + twinflowTransitionTriggerId + ", userId:" + createdByUserId + ", businessAccountId:" + businessAccountId + "]";
            default -> "twinflowTransitionTriggerTask[id:" + id + "]";
        };
    }
}
