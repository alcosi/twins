package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_link")
@FieldNameConstants
public class TwinLinkEntity implements PublicCloneable<TwinLinkEntity>, EasyLoggable, Identifiable {
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

    // Redundant by design: == id. Hosts the FK to twin(id) for referential integrity + reverse cascade;
    // the value equals the relation twin id, which (ID equality) equals this twin_link id.
    @Column(name = "relation_twin_id")
    private UUID relationTwinId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity srcTwinSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity srcTwin;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity dstTwinSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity dstTwin;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", insertable = false, updatable = false, nullable = false)
    private LinkEntity linkSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private LinkEntity link;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_twin_id", insertable = false, updatable = false)
    private TwinEntity relationTwinSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity relationTwin;

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
                .setRelationTwinId(relationTwinId)
                .setRelationTwin(relationTwin)
                .setCreatedByUserId(createdByUserId)
                .setCreatedByUser(createdByUser);
    }
}
