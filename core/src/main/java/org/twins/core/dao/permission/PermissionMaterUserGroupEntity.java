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
@IdClass(PermissionMaterUserGroupEntity.Pk.class)
@Table(name = "permission_mater_user_group")
public class PermissionMaterUserGroupEntity implements EasyLoggable {
    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Id
    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Id
    @Column(name = "user_group_footprint_id")
    private UUID userGroupFootprintId;

    @Column(name = "grants_count")
    private Integer grantsCount;

    public record Pk(
            UUID permissionSchemaId,
            UUID permissionId,
            UUID userGroupFootprintId
    ) implements Serializable {}

    public String easyLog(Level level) {
        return "permissionGrantUserGroup[permissionSchemaId:" + permissionSchemaId + ", permissionId:" + permissionId + ", userGroupFootprintId:" + userGroupFootprintId + "]";
    }
}
