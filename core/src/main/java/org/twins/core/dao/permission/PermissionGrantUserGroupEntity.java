package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "permission_grant_user_group")
public class PermissionGrantUserGroupEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

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
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false, nullable = false)
    private PermissionEntity permissionSpecOnly;

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
    @JoinColumn(name = "granted_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity grantedByUserSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity permission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserGroupEntity userGroup;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity grantedByUser;

    public String easyLog(Level level) {
        return "permissionGrantUserGroup[id:" + id + ", permissionSchemaId:" + permissionSchemaId + ", permissionId:" + permissionId + ", userGroupId:" + userGroupId + "]";
    }
}
