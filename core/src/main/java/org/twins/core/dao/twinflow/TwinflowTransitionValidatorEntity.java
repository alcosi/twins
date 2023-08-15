package org.twins.core.dao.twinflow;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.cambium.featurer.annotations.FeaturerList;

import jakarta.persistence.*;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.featurer.transition.validator.TransitionValidator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinflow_transition_validator")
public class TwinflowTransitionValidatorEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_transition_id")
    private UUID twinflowTransitionId;

    @Column(name = "order")
    private UUID order;

    @Column(name = "transition_validator_featurer_id")
    private Integer transitionValidatorFeaturerId;

    @FeaturerList(type = TransitionValidator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transition_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity transitionValidatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "transition_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> transitionValidatorParams;
}
