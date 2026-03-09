package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Immutable
@IdClass(PermissionMaterSpaceUserEntity.Pk.class)
@Table(name = "permission_mater_space_user")
public class PermissionMaterSpaceUserEntity implements EasyLoggable {
    @Id
    @Column(name = "twin_id")
    private UUID twinId;

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Id
    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "grants_count")
    private Integer grantsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private PermissionEntity permission;

    public record Pk(
            UUID twinId,
            UUID permissionSchemaId,
            UUID permissionId,
            UUID userId
    ) implements Serializable {}

    public String easyLog(Level level) {
        return "permissionMaterSpaceUser[twinId:" + twinId + ", permissionSchemaId:" + permissionSchemaId + ", permissionId:" + permissionId + ", userId:" + userId + "]";
    }
}
