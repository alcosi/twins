package org.twins.core.dao.twinflow;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition_trigger")
public class TwinflowTransitionTriggerEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "transition_trigger_featurer_id")
    private Integer transitionTriggerFeaturerId;

    @FeaturerList(type = TransitionTrigger.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transition_trigger_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity transitionTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "transition_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> transitionTriggerParams;

    @Column(name = "active")
    private boolean isActive;

    public String easyLog(EasyLoggable.Level level) {
        switch (level) {
            case SHORT:
                return "twinflowTransitionTrigger[" + id + ", isActive: " + isActive + "]";
            case NORMAL:
                return "twinflowTransitionTrigger[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId + ", isActive: " + isActive +  "]";
            default:
                return "twinflowTransitionTrigger[id:" + id + ", twinflowTransitionId:" + twinflowTransitionId +  ", order:" + order +  ", featurer:" + transitionTriggerFeaturerId + ", isActive: " + isActive + "]";
        }
    }
}
