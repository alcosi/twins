package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.factory.TwinFactoryTaskStatus;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_change_task")
@FieldNameConstants
public class TwinChangeTaskEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "input_twin_id")
    private UUID inputTwinId;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_factory_task_status_id")
    private TwinFactoryTaskStatus statusId;

    @Column(name = "twin_factory_task_status_details")
    private String statusDetails;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "done_at")
    private Timestamp doneAt;

    @ManyToOne
    @JoinColumn(name = "input_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity inputTwin;

    @Override
    public String easyLog(Level level) {
        return "twinFactoryTask[id:" + id + "]";
    }
}
