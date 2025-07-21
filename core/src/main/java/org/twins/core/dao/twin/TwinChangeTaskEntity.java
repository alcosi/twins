package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.domain.factory.FactoryLauncher;

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

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_factory_launcher_id")
    private FactoryLauncher twinFactorylauncher;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_change_task_status_id")
    private TwinChangeTaskStatus statusId;

    @Column(name = "twin_change_task_status_details")
    private String statusDetails;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "done_at")
    private Timestamp doneAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case NORMAL -> "twinChangeTask[id:" + id + ", twinId:" + twinId + ", factoryId:" + twinFactoryId + "]";
            case DETAILED ->
                    "twinChangeTask[id:" + id + ", twinId:" + twinId + ", factoryId:" + twinFactoryId + ", userId:" + createdByUserId + ", businessAccountId:" + businessAccountId + "]";
            default -> "twinChangeTask[id:" + id + "]";
        };
    }
}
