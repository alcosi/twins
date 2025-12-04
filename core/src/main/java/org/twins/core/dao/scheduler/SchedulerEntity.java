package org.twins.core.dao.scheduler;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "scheduler")
@Accessors(chain = true)
@Data
public class SchedulerEntity implements EasyLoggable {

    @Id
    private UUID id;

    @Column(name = "scheduler_featurer_id")
    private int featurerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "scheduler_params", columnDefinition = "hstore")
    private HashMap<String, String> schedulerParams;

    @Column(name = "active")
    private boolean active;

    @Column(name = "log_enabled")
    private boolean logEnabled;

    @Column(name = "cron")
    private String cron;

    @Column(name = "fixed_rate")
    private Integer fixedRate;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;


    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT ->
                    STR."scheduleEntity[id:\{id}]";
            case NORMAL ->
                    STR."scheduleEntity[id:\{id}, featurerId:\{featurerId}, description:\{description}, active:\{active}]";
            case DETAILED ->
                    STR."scheduleEntity[id:\{id}, featurerId:\{featurerId}, description:\{description}, active:\{active}, logEnabled:\{logEnabled}, cron:\{cron}, fixedRate:\{fixedRate}, params:\{schedulerParams.entrySet().stream().filter(it -> it.getValue() != null).map(Map.Entry::getKey).collect(Collectors.joining(","))}]";
        };
    }
}
