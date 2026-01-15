package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_link")
@FieldNameConstants
public class TwinLinkEntity implements PublicCloneable<TwinLinkEntity>, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "src_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity srcTwin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "dst_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity dstTwin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "link_id", insertable = false, updatable = false, nullable = false)
    private LinkEntity link;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinLink[" + id + "]";
            case NORMAL -> "twinLink[id:" + id + ", linkId:" + linkId + "]";
            default ->
                    "twinLink[id:" + id + ", linkId:" + linkId + ", srcTwinId:" + srcTwinId + ", dstTwinId:" + dstTwinId + "]";
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
