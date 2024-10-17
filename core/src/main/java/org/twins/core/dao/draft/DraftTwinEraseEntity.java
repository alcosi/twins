package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.Hibernate;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.io.Serial;
import java.util.Arrays;
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
    @Convert(converter = DraftTwinEraseReasonConverter.class)
    private Reason reason;

    @Column(name = "reason_twin_id")
    private UUID reasonTwinId;

    @Column(name = "reason_link_id")
    private UUID reasonLinkId;

    @Column(name = "draft_twin_erase_status_id")
    @Convert(converter = DraftTwinEraseStatusConverter.class)
    private Status status;

    @Column(name = "status_details")
    private String statusDetails;

    @Column(name = "erase_twin_status_id")
    private UUID eraseTwinStatusId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "draft_id", insertable = false, updatable = false)
    private DraftEntity draft;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private TwinEntity twin;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "erase_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity eraseTwinStatus;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "draftTwinErase[draftId:" + draftId + ", twinId:" + twinId + "]";
            case NORMAL -> "draftTwinErase[draftId:" + draftId + ", twinId:" + twinId + ", reason:" + reason + "]";
            default -> "draftTwinErase[draftId:" + draftId + ", twinId:" + twinId + ", reason:" + reason + "reasonTwinId:" + reasonTwinId + "]";
        };
    }

    public boolean isEraseReady() {
        return status != Status.UNDETECTED;
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

    @Getter
    public enum Reason {
        TARGET("TARGET"),
        CHILD("CHILD"),
        LINK("LINK"),
        FACTORY("FACTORY");

        private final String id;

        Reason(String id) {
            this.id = id;
        }

        public static Reason valueOd(String type) {
            return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElseThrow();
        }

    }

    @Getter
    public enum Status {
        // we do not know what should be done with current twin. so we will run target delete factory for it
        UNDETECTED("UNDETECTED"),
        // current twin must be deleted. but we still do run cascade erase children and string links
        IRREVOCABLE_ERASE_DETECTED("IRREVOCABLE_ERASE_DETECTED"),
        IRREVOCABLE_ERASE_HANDLED("IRREVOCABLE_ERASE_HANDLED"),
        CASCADE_DELETION_PAUSE("CASCADE_DELETION_PAUSE"),
        CASCADE_DELETION_EXTRACTION("CASCADE_DELETION_EXTRACTION"),
        // current twin must be deleted. and we already process cascade erase for children and string links

//        DETECTED_STATUS_CHANGE_ERASE("DETECTED_STATUS_CHANGE_ERASE"),
//        DETECTED_SKIP("DETECTED_SKIP"),
        // current twin locks deletion
        DETECTED_LOCK("DETECTED_LOCK");

        private final String id;

        Status(String id) {
            this.id = id;
        }

        public static Status valueOd(String type) {
            return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElseThrow();
        }

    }

}