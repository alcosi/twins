package org.twins.core.dao.scheduler;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "scheduler")
@Accessors(chain = true)
@FieldNameConstants
@Data
public class SchedulerEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "scheduler_featurer_id")
    private Integer featurerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "scheduler_params", columnDefinition = "hstore")
    private HashMap<String, String> schedulerParams;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "log_enabled")
    private Boolean logEnabled;

    @Column(name = "cron")
    private String cron;

    @Column(name = "fixed_rate")
    private Integer fixedRate;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity featurer;


    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT ->
                    "scheduler[id:" + id + "]";
            case NORMAL ->
                    "scheduler[id:" + id + ", featurerId:" + featurerId + ", description:" + description + ", active:" + active + "]";
            case DETAILED ->
                    "scheduler[id:" + id + ", featurerId:" + featurerId + ", description:" + description + ", active:" + active + ", logEnabled:" + logEnabled + ", cron:" + cron + ", fixedRate:" + fixedRate + ", params:" + schedulerParams.entrySet().stream().filter(it -> it.getValue() != null).map(Map.Entry::getKey).collect(Collectors.joining(",")) + "]";
        };
    }
}
