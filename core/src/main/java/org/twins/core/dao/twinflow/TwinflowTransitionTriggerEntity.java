package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.trigger.TwinTriggerEntity;

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

    @Column(name = "twin_trigger_id")
    private UUID twinTriggerId;

    @Column(name = "async")
    private Boolean async;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_transition_id", insertable = false, updatable = false)
    private TwinflowTransitionEntity twinflowTransitionSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowTransitionEntity twinflowTransition;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_trigger_id", insertable = false, updatable = false)
    private TwinTriggerEntity twinTriggerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinTriggerEntity twinTrigger;

    @Column(name = "active")
    private Boolean active;

    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinflowTransitionTrigger[" + id + "]";
            case NORMAL ->
                    "twinflowTransitionTrigger[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", active: " + active + "]";
            default ->
                    "twinflowTransitionTrigger[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", order:" + order + ", twinTriggerId:" + twinTriggerId + ", active: " + active + "]";
        };
    }

    @Override
    public TwinflowTransitionTriggerEntity clone() {
        return new TwinflowTransitionTriggerEntity()
                .setTwinflowTransitionId(twinflowTransitionId)
                .setOrder(order)
                .setTwinTriggerId(twinTriggerId)
                .setAsync(async)
                .setActive(active);
    }
}
