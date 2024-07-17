package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "twin_eraser_transaction_scope")
@IdClass(TwinEraserTransactionScopeEntity.PK.class)
public class TwinEraserTransactionScopeEntity {
    @Id
    @Column(name = "twin_eraser_transaction_id", nullable = false)
    private UUID twinEraserTransactionId;

    @Id
    @Column(name = "twin_id", nullable = false)
    private UUID twinId;

    @Column(name = "self_scope_loaded")
    private boolean selfScopeLoaded;

    @Column(name = "reason_twin_id")
    private UUID reasonTwinTd;

    @Column(name = "twin_eraser_reason_id")
    @Convert(converter = TwinEraserReasonConverter.class)
    private Reason reason;

    @Column(name = "erase_twin_status_id")
    private UUID eraseTwinStatusId;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;


    @Data
    public static class PK implements java.io.Serializable {
        @Serial
        private static final long serialVersionUID = -8233976855305006652L;
        private UUID twinEraserTransactionId;

        private UUID twinId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            PK entity = (PK) o;
            return Objects.equals(this.twinId, entity.twinId) &&
                    Objects.equals(this.twinEraserTransactionId, entity.twinEraserTransactionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(twinId, twinEraserTransactionId);
        }

    }

    @Getter
    public enum Reason {
        TARGET("TARGET"),
        CHILD("CHILD"),
        LINK("LINK"),
//        TARGET_CHILD("TARGET_CHILD"),
//        TARGET_LINK("TARGET_LINK"),
//        TARGET_CHILD_LINK("TARGET_CHILD_LINK"),
//        TARGET_LINK_CHILD("TARGET_LINK_CHILD"),
        UNKNOWN("UNKNOWN");

        private final String id;

        Reason(String id) {
            this.id = id;
        }

        public static Reason valueOd(String type) {
            return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElse(UNKNOWN);
        }

    }
}