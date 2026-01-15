package org.twins.core.dao.twinflow;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition_trigger")
@FieldNameConstants
public class TwinflowTransitionTriggerEntity implements EasyLoggable, PublicCloneable<TwinflowTransitionTriggerEntity> {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "transition_trigger_featurer_id")
    private Integer transitionTriggerFeaturerId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity transitionTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "transition_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> transitionTriggerParams;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twinflow_transition_id", insertable = false, updatable = false)
    private TwinflowTransitionEntity twinflowTransition;

    @Column(name = "active")
    private Boolean isActive;

    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinflowTransitionTrigger[" + id + "]";
            case NORMAL ->
                    "twinflowTransitionTrigger[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", isActive: " + isActive + "]";
            default ->
                    "twinflowTransitionTrigger[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", order:" + order + ", featurer:" + transitionTriggerFeaturerId + ", isActive: " + isActive + "]";
        };
    }

    @Override
    public TwinflowTransitionTriggerEntity clone() {
        return new TwinflowTransitionTriggerEntity()
                .setTwinflowTransitionId(twinflowTransitionId)
                .setOrder(order)
                .setTransitionTriggerFeaturerId(transitionTriggerFeaturerId)
                .setTransitionTriggerFeaturer(transitionTriggerFeaturer)
                .setTransitionTriggerParams(transitionTriggerParams)
                .setIsActive(isActive);
    }
}
