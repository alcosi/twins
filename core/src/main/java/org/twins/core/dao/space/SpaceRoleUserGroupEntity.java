package org.twins.core.dao.space;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
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
    @GeneratedValue(generator = "uuid")
    private UUID id;

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "space_role_id", insertable = false, updatable = false, nullable = false)
    private SpaceRoleEntity spaceRole;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity userGroupByUserGroupId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
