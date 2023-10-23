package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_link")
public class TwinLinkEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "src_twin_id")
    private UUID srcTwinId;

    @Column(name = "dst_twin_id")
    private UUID dstTwinId;

    @Column(name = "link_id")
    private UUID linkId;

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
    @JoinColumn(name = "link_id", insertable = false, updatable = false, nullable = false)
    private LinkEntity link;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
