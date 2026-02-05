package org.twins.core.dao.factory;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@DomainSetting
@Table(name = "twin_factory_pipeline_step")
public class TwinFactoryPipelineStepEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_factory_pipeline_id")
    private UUID twinFactoryPipelineId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

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
    private Integer fillerFeaturerId;

    @Column(name = "description")
    private String description;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "filler_params", columnDefinition = "hstore")
    private HashMap<String, String> fillerParams;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity fillerFeaturer;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_pipeline_id", insertable = false, updatable = false)
    private TwinFactoryPipelineEntity twinFactoryPipeline;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity twinFactoryConditionSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryPipelineStep[" + id + "]";
            case NORMAL -> "twinFactoryPipelineStep[" + id + "] **" + description + "**";
            default ->
                    "twinFactoryPipelineStep[id:" + id + ", twinFactoryPipelineId:" + twinFactoryPipelineId + ", comment:"
                            + description + "]";
        };

    }
}
