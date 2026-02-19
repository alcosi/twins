package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.trigger.TwinTriggerEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_status_transition_trigger")
public class TwinStatusTransitionTriggerEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "twin_status_transition_type_id")
    @Enumerated(EnumType.STRING)
    private TransitionType type;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "twin_trigger_id")
    private UUID twinTriggerId;

    @Column(name = "async")
    private Boolean async;

    @Column(name = "active")
    private Boolean active;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_trigger_id", insertable = false, updatable = false)
    private TwinTriggerEntity twinTrigger;

    public enum TransitionType {
        incoming, outgoing;
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinStatusTransitionTrigger[" + id + "]";
            case NORMAL -> "twinStatusTransitionTrigger[id:" + id + ", statusId:" + twinStatusId + ", type:" + type + "]";
            default -> "twinStatusTransitionTrigger[id:" + id + ", statusId:" + twinStatusId + ", type:" + type + ", order:" + order + ", twinTriggerId:" + twinTriggerId + "]";
        };
    }
}
