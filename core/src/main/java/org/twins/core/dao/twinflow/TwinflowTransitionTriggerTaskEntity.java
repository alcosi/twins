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
    /*
    (
    id uuid NOT NULL,
    twin_id uuid NOT NULL,
    request_id uuid NOT NULL,
    twinflow_transition_trigger_id uuid NOT NULL,
    src_twin_status_id varchar(20) NOT NULL,
    created_by_user_id uuid NOT NULL,
    business_account_id uuid NULL,
    twinflow_transition_trigger_task_status_id varchar(20) NOT NULL,
    twinflow_transition_trigger_task_status_details varchar NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    done_at timestamp NULL,
    CONSTRAINT twinflow_transition_trigger_task_pk PRIMARY KEY (id),
    CONSTRAINT twinflow_transition_trigger_task_pk_2 UNIQUE (twin_id, request_id),
    CONSTRAINT twinflow_transition_trigger_task_business_account_id_fk FOREIGN KEY (business_account_id) REFERENCES public.business_account(id),
    CONSTRAINT twinflow_transition_trigger_task_created_by_user_id_fk FOREIGN KEY (created_by_user_id) REFERENCES public."user"(id),
    CONSTRAINT twinflow_transition_trigger_task_task_status_id_fk FOREIGN KEY (twin_change_task_status_id) REFERENCES public.twin_change_task_status(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT twinflow_transition_trigger_task_trigger_id_fk FOREIGN KEY (twinflow_transition_trigger_id) REFERENCES public.twinflow_transition_trigger(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT twinflow_transition_trigger_twin_id_fk FOREIGN KEY (twin_id) REFERENCES public.twin(id)
    );
CREATE INDEX twinflow_transition_trigger_task_business_account_id_index ON public.twinflow_transition_trigger_task USING btree (business_account_id);
CREATE INDEX twinflow_transition_trigger_task_created_by_user_id_index ON public.twinflow_transition_trigger_task USING btree (created_by_user_id);
CREATE INDEX twinflow_transition_trigger_task_input_twin_id_index ON public.twinflow_transition_trigger_task USING btree (twin_id);
CREATE INDEX twinflow_transition_trigger_task_twin_status_id_index ON public.twinflow_transition_trigger_task USING btree (twinflow_transition_trigger_task_status_id);
CREATE UNIQUE INDEX twinflow_transition_trigger_task_twin_id_request_id_uindex ON public.twinflow_transition_trigger_task USING btree (twin_id, request_id);
);
     */
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
