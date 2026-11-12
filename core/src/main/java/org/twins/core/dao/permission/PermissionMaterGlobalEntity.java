package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Immutable;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Immutable
@IdClass(PermissionMaterGlobalEntity.Pk.class)
@Table(name = "permission_mater_global")
@DataScope(DataScopeType.DOMAIN_MEMBER)
public class PermissionMaterGlobalEntity implements EasyLoggable {
    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Id
    @Column(name = "user_group_footprint_id")
    private UUID userGroupFootprintId;

    @Column(name = "grants_count")
    private Integer grantsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private PermissionEntity permission;

    public record Pk(
            UUID permissionId,
            UUID userGroupFootprintId
    ) implements Serializable {}

    public String easyLog(EasyLoggable.Level level) {
        return "permissionMatGlobal[permissionId:" + permissionId + ", userGroupFootprintId:" + userGroupFootprintId + "]";
    }
}