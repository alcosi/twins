package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.twins.core.dao.twin.TwinEntity;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_erase")
@IdClass(DraftTwinEraseEntity.PK.class)
public class DraftTwinEraseEntity {
    @Id
    @Column(name = "draft_id")
    private UUID draftId;

    @Id
    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "erase_ready")
    private boolean eraseReady = false;

    @Column(name = "reason_twin_id")
    private UUID reasonTwinId;

    @Column(name = "twin_erase_reason_id")
    @Convert(converter = DraftTwinEraseReasonConverter.class)
    private Reason reason;

    @Column(name = "erase_twin_status_id")
    private UUID eraseTwinStatusId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id")
    private DraftEntity draft;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "twin_id")
    private TwinEntity twin;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

}