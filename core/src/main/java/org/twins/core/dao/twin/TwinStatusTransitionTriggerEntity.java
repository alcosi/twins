package org.twins.core.dao.twin;

import com.github.f4b6a3.uuid.UuidCreator;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_status_transition_trigger")
public class TwinStatusTransitionTriggerEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "twin_status_transition_type_id")
    @Enumerated(EnumType.STRING)
    private TransitionType type;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "transition_trigger_featurer_id")
    private Integer transitionTriggerFeaturerId;

    @Column(name = "active")
    private boolean active;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity transitionTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "transition_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> transitionTriggerParams;

    public enum TransitionType {
        incoming, outgoing;
    }

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinStatusTransitionTrigger[" + id + "]";
            case NORMAL:
                return "twinStatusTransitionTrigger[id:" + id + ", statusId:" + twinStatusId + ", type:" + type + "]";
            default:
                return "twinStatusTransitionTrigger[id:" + id + ", statusId:" + twinStatusId + ", type:" + type + ", order:" + order + ", featurer:" + transitionTriggerFeaturerId + "]";
        }
    }
}
