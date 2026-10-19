package org.twins.core.dao.space;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "space_role_user_group")
@FieldNameConstants
public class SpaceRoleUserGroupEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "space_role_id")
    private UUID spaceRoleId;

    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twinSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_role_id", insertable = false, updatable = false, nullable = false)
    private SpaceRoleEntity spaceRoleSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity userGroupSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUserSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserGroupEntity userGroup;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity twin;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private SpaceRoleEntity spaceRole;
}
