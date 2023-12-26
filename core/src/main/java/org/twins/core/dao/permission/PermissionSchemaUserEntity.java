package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "permission_schema_user")
public class PermissionSchemaUserEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "granted_by_user_id")
    private UUID grantedByUserId;

    @Column(name = "granted_at")
    private Timestamp grantedAt;

    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "permission_id", insertable = false, updatable = false, nullable = false)
    private PermissionEntity permission;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "granted_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity grantedByUser;
}
