package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.factory.multiplier.Multiplier;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory_pipeline_step")
public class TwinFactoryPipelineStepEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_pipeline_id")
    private UUID twinFactoryPipelineId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private Boolean twinFactoryConditionInvert;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "optional")
    private Boolean optional;

    @Column(name = "filler_featurer_id")
    private int fillerFeaturerId;

    @Column(name = "description")
    private String description;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "filler_params", columnDefinition = "hstore")
    private HashMap<String, String> fillerParams;

    @FeaturerList(type = Multiplier.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "filler_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity fillerFeaturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_pipeline_id", insertable = false, updatable = false)
    private TwinFactoryPipelineEntity twinFactoryPipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity twinFactoryConditionSet;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryPipelineStep[" + id + "]";
            case NORMAL -> "twinFactoryPipelineStep[" + id + "] **" + description + "**";
            default ->
                    "twinFactoryPipelineStep[id:" + id + ", twinFactoryPipelineId:" + twinFactoryPipelineId + ", comment:" + description + "]";
        };

    }
}
