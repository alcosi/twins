package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "tier")
public class TierEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "custom")
    private Boolean custom;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "attachments_storage_quota_count")
    private Integer attachmentsStorageQuotaCount;

    @Column(name = "attachments_storage_quota_size")
    private Long attachmentsStorageQuotaSize;

    @Column(name = "user_count_quota")
    private Integer userCountQuota;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    //Performance safe because tier is not used in operations
    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "twinflow_schema_id", insertable = false, updatable = false)
    private TwinflowSchemaEntity twinflowSchema;

    @ManyToOne
    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false)
    private TwinClassSchemaEntity twinClassSchema;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "TierEntity[id:" + id + ", name: " + name + "]";
            case NORMAL -> "TierEntity[id:" + id + ", name: " + name + ", custom: " + custom + "]";
            default -> "TierEntity[id:" + id + ", name: " + name + ", custom: " + custom + ", attachmentsStorageQuotaCount: " + attachmentsStorageQuotaCount + ", attachmentsStorageQuotaSize: " + attachmentsStorageQuotaSize + ", userCountQuota: " + userCountQuota + "]";
        };
    }

}
