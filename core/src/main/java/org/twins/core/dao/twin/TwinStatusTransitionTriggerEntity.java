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
@Table(name = "twin_status_trigger")
public class TwinStatusTransitionTriggerEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "incoming_else_outgoing")
    private Boolean incomingElseOutgoing;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "twin_trigger_id")
    private UUID twinTriggerId;

    @Column(name = "async")
    private Boolean async;

    @Column(name = "active")
    private Boolean active;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinStatusEntity twinStatus;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinTriggerEntity twinTrigger;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinStatusTrigger[" + id + "]";
            case NORMAL -> "twinStatusTrigger[id:" + id + ", statusId:" + twinStatusId + ", incoming:" + incomingElseOutgoing + "]";
            default -> "twinStatusTrigger[id:" + id + ", statusId:" + twinStatusId + ", incoming:" + incomingElseOutgoing + ", order:" + order + ", twinTriggerId:" + twinTriggerId + "]";
        };
    }
}
