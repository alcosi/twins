package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.Hibernate;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.draft.DraftTwinEraseReason;
import org.twins.core.enums.draft.DraftTwinEraseStatus;

import java.io.Serial;
import java.util.Objects;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_erase")
@IdClass(DraftTwinEraseEntity.PK.class)
public class DraftTwinEraseEntity implements EasyLoggable {
    @Id
    @Column(name = "draft_id")
    private UUID draftId;

    @Id
    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "time_in_millis")
    private long timeInMillis;

    @Column(name = "twin_erase_reason_id")
    @Enumerated(EnumType.STRING)
    private DraftTwinEraseReason reason;

    @Column(name = "reason_twin_id")
    private UUID reasonTwinId;

    @Column(name = "reason_link_id")
    private UUID reasonLinkId;

    @Column(name = "draft_twin_erase_status_id")
    @Enumerated(EnumType.STRING)
    private DraftTwinEraseStatus status;

    @Column(name = "cascade_break_twin_id")
    private UUID cascadeBreakTwinId;

    @Column(name = "status_details")
    private String statusDetails;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "draft_id", insertable = false, updatable = false)
    private DraftEntity draft;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private TwinEntity twin;


    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "draftTwinErase[draftId:" + draftId + ", twinId:" + twinId + "]";
            case NORMAL -> "draftTwinErase[draftId:" + draftId + ", twinId:" + twinId + ", reason:" + reason + "]";
            default -> "draftTwinErase[draftId:" + draftId + ", twinId:" + twinId + ", reason:" + reason + "reasonTwinId:" + reasonTwinId + "]";
        };
    }

    public boolean isEraseReady() {
        return status != DraftTwinEraseStatus.UNDETECTED;
    }
//
//    @ManyToOne(fetch = FetchType.EAGER, optional = false)
//    @JoinColumn(name = "reason_twin_id")
//    private TwinEntity reasonTwin;

    @Data
    public static class PK implements java.io.Serializable {
        @Serial
        private static final long serialVersionUID = -8233976855305006652L;
        private UUID draftId;

        private UUID twinId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            DraftTwinEraseEntity.PK entity = (DraftTwinEraseEntity.PK) o;
            return Objects.equals(this.twinId, entity.twinId) &&
                    Objects.equals(this.draftId, entity.draftId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(twinId, draftId);
        }
    }

}
