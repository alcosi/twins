package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_factory_condition_set")
public class TwinFactoryConditionSetEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "cachable")
    private Boolean cachable;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactorySpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinFactoryEntity twinFactory;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    // Trigger-maintained usage-counter columns (created in V1.4.327.03, maintained by AFTER
    // triggers). insertable=false/updatable=false keeps Hibernate out of the write path
    // (otherwise INSERT sends NULL into a NOT NULL DEFAULT 0 column, and UPDATE clobbers the
    // trigger); @Generated makes Hibernate re-read the row after INSERT/UPDATE. usage_count_trigger
    // is also maintained by V1.4.327.03 triggers.
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count_pipeline", insertable = false, updatable = false)
    private Integer usageCountPipeline;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count_pipeline_step", insertable = false, updatable = false)
    private Integer usageCountPipelineStep;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count_multiplier_filter", insertable = false, updatable = false)
    private Integer usageCountMultiplierFilter;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count_branch", insertable = false, updatable = false)
    private Integer usageCountBranch;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count_eraser", insertable = false, updatable = false)
    private Integer usageCountEraser;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "usage_count_trigger", insertable = false, updatable = false)
    private Integer usageCountTrigger;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFactoryConditionEntity, UUID> twinFactoryConditionKit;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryConditionSet[" + id + "]";
            default -> "twinFactoryConditionSet[id:" + id + ", domainId:" + domainId + "]";
        };
    }
}
