package org.twins.core.dao.twin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "twin_eraser_transaction")
public class TwinEraserTransactionEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "created_by_user_id")
    private UUID createdByUser;

    @Column(name = "twins_count")
    private int twinsCount;

    @Column(name = "commited")
    private boolean commited;

    @Column(name = "created_at")
    private Timestamp createdAt;

}