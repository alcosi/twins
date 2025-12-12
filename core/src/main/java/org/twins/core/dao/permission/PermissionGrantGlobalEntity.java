package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Table(name = "permission_grant_global")
public class PermissionGrantGlobalEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Column(name = "granted_by_user_id")
    private UUID grantedByUserId;

    @Column(name = "granted_at")
    private Timestamp grantedAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "permission_id", insertable = false, updatable = false, nullable = false)
    private PermissionEntity permission;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity userGroup;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "granted_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity grantedByUser;
}
