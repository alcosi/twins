package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_link")
@FieldNameConstants
public class TwinLinkEntity implements PublicCloneable<TwinLinkEntity>, TwinFieldStorage, EasyLoggable {
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

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
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

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinLink[" + id + "]";
            case NORMAL -> "twinLink[id:" + id + ", linkId:" + linkId + "]";
            default -> "twinLink[id:" + id + ", linkId:" + linkId + ", srcTwinId:" + srcTwinId + ", dstTwinId:" + dstTwinId + "]";
        };
    }

    @Transient
    private boolean uniqForSrcRelink = true;

    public TwinLinkEntity clone() {
        return new TwinLinkEntity()
                .setDstTwinId(dstTwinId)
                .setDstTwin(dstTwin)
                .setLinkId(linkId)
                .setLink(link)
                .setSrcTwinId(srcTwinId)
                .setSrcTwin(srcTwin)
                .setCreatedByUserId(createdByUserId);
    }
}
