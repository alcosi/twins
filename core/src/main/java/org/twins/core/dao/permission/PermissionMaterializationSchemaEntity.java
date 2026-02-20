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
@Table(name = "permission_materialization_schema_level")
@IdClass(PermissionMaterializationSchemaEntity.PK.class)
public class PermissionMaterializationSchemaEntity implements EasyLoggable {

    @Id
    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "grants_count")
    private int grantsCount;

    public String easyLog(Level level) {
        return "permissionMaterializationSchema[schemaId:" + permissionSchemaId + ", permissionId:" + permissionId + ", userId:" + userId + "]";
    }

    @Data
    public static class PK implements Serializable {
        private UUID permissionSchemaId;
        private UUID permissionId;
        private UUID userId;
    }
}
