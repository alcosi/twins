package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinRole;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "permission_grant_twin_role")
public class PermissionGrantTwinRoleEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_role_id")
    @Enumerated(EnumType.STRING)
    private TwinRole twinRole;

    @Column(name = "granted_by_user_id")
    private UUID grantedByUserId;

    @Column(name = "granted_at")
    private Timestamp grantedAt;

    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @JoinColumn(name = "permission_id", insertable = false, updatable = false, nullable = false)
    private PermissionEntity permission;

    @ManyToOne
    @JoinColumn(name = "granted_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity grantedByUser;
}
