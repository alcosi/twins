package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Immutable
@IdClass(PermissionMaterGlobalEntity.Pk.class)
@Table(name = "permission_mater_global")
public class PermissionMaterGlobalEntity implements EasyLoggable {
    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Id
    @Column(name = "user_group_footprint_id")
    private UUID userGroupFootprintId;

    @Column(name = "grants_count")
    private Integer grantsCount;

    public record Pk(
            UUID permissionId,
            UUID userGroupFootprintId
    ) implements Serializable {}

    public String easyLog(EasyLoggable.Level level) {
        return "permissionMatGlobal[permissionId:" + permissionId + ", userGroupFootprintId:" + userGroupFootprintId + "]";
    }
}