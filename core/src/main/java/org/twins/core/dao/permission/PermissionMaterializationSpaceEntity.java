package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "permission_materialization_space_level")
@IdClass(PermissionMaterializationSpaceEntity.PK.class)
public class PermissionMaterializationSpaceEntity implements EasyLoggable {

    @Id
    @Column(name = "twin_id")
    private UUID twinId;

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "grants_count")
    private int grantsCount;

    public String easyLog(Level level) {
        return "permissionMaterializationSpace[twinId:" + twinId + ", permissionId:" + permissionId + ", userId:" + userId + "]";
    }

    @Data
    public static class PK implements Serializable {
        private UUID twinId;
        private UUID permissionId;
        private UUID userId;
    }
}
