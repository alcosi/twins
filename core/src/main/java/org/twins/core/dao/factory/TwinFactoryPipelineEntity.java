package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_factory_pipeline")
public class TwinFactoryPipelineEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private Boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "next_twin_factory_id")
    private UUID nextTwinFactoryId;

    @Column(name = "after_commit_twin_factory_id")
    private UUID afterCommitTwinFactoryId;

    @Column(name = "next_twin_factory_limit_scope")
    private Boolean nextTwinFactoryLimitScope;

    @Column(name = "template_twin_id")
    private UUID templateTwinId;

    @Column(name = "description")
    private String description;

    @Column(name = "output_twin_status_id")
    private UUID outputTwinStatusId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity nextTwinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClass;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity conditionSet;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "output_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity outputTwinStatus;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "template_twin_id", insertable = false, updatable = false, nullable = true)
    private TwinEntity templateTwin;

    @Transient
    private Integer pipelineStepsCount;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryPipeline[" + id + "]";
            default -> "twinFactoryPipeline[id:" + id + ", twinFactoryId:" + twinFactoryId + ", inputTwinClassId:" + inputTwinClassId + "]";
        };

    }
}
