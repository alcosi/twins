package org.twins.core.dao.twinlink;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "twinlink_map")
public class TwinlinkMapEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "src_twin_id")
    private UUID srcTwinId;

    @Column(name = "dst_twin_id")
    private UUID dstTwinId;

    @Column(name = "twinlink_id")
    private UUID twinlinkId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "src_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity srcTwin;

    @ManyToOne
    @JoinColumn(name = "dst_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity dstTwin;

    @ManyToOne
    @JoinColumn(name = "twinlink_id", insertable = false, updatable = false, nullable = false)
    private TwinlinkEntity twinlink;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
