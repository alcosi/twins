package org.twins.core.dao.twinflow;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinflow_transition_trigger")
public class TwinflowTransitionTriggerEntity {
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
}
