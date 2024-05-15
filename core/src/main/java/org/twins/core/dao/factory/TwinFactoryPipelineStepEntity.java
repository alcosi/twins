package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.factory.multiplier.Multiplier;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "twin_factory_pipeline_step")
@Accessors(chain = true)
@Data
public class TwinFactoryPipelineStepEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_pipeline_id")
    private UUID twinFactoryPipelineId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private boolean twinFactoryConditionInvert;

    @Column(name = "order")
    private int order;

    @Column(name = "active")
    private boolean active;

    @Column(name = "optional")
    private boolean optional;

    @Column(name = "filler_featurer_id")
    private int fillerFeaturerId;

    @Column(name = "comment")
    private String comment;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "filler_params", columnDefinition = "hstore")
    private HashMap<String, String> fillerParams;

    @FeaturerList(type = Multiplier.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "filler_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity fillerFeaturer;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryPipelineStep[" + id + "]";
            case NORMAL -> "twinFactoryPipelineStep[" + id + "] **" + comment + "**";
            default -> "twinFactoryPipelineStep[id:" + id + ", twinFactoryPipelineId:" + twinFactoryPipelineId + ", comment:" + comment + "]";
        };

    }
}
