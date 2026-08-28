package org.twins.core.dao.scheduler;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "scheduler_log")
@Accessors(chain = true)
@Data
@FieldNameConstants
public class SchedulerLogEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "scheduler_id")
    private UUID schedulerId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "result")
    private String result;

    @Column(name = "execution_time")
    private long executionTime;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private SchedulerEntity scheduler;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduler_id", insertable = false, updatable = false)
    private SchedulerEntity schedulerSpecOnly;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT ->
                    "schedulerLog[id:" + id + "]";
            case NORMAL ->
                    "schedulerLog[id:" + id + ", schedulerId:" + schedulerId + ", result:" + result + "]";
            case DETAILED ->
                    "schedulerLog[id:" + id + ", schedulerId:" + schedulerId + ", result:" + result + ", createdAt:" + createdAt + ", executionTime:" + executionTime + "]";
        };
    }
}
