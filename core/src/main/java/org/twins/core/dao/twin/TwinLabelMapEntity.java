package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_label_map")
public class TwinLabelMapEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_label_id")
    private UUID twinLabelId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twinByTwinId;

    @ManyToOne
    @JoinColumn(name = "twin_label_id", insertable = false, updatable = false, nullable = false)
    private TwinLabelEntity twinLabelByTwinLabelId;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
