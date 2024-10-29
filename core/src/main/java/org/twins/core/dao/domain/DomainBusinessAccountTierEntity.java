package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_business_account_tier")
public class DomainBusinessAccountTierEntity implements EasyLoggable {

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
    private boolean custom;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "attachments_storage_quota_count")
    private Integer attachmentsStorageQuotaCount;

    @Column(name = "attachments_storage_quota_size")
    private Integer attachmentsStorageQuotaSize;

    @Column(name = "user_count_quota")
    private Integer userCountQuota;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "DomainBusinessAccountTierEntity[id:" + id + ", name: " + name + "]";
            case NORMAL -> "DomainBusinessAccountTierEntity[id:" + id + ", name: " + name + ", custom: " + custom + "]";
            default -> "DomainBusinessAccountTierEntity[id:" + id + ", name: " + name + ", custom: " + custom + ", attachmentsStorageQuotaCount: " + attachmentsStorageQuotaCount + ", attachmentsStorageQuotaSize: " + attachmentsStorageQuotaSize + ", userCountQuota: " + userCountQuota + "]";
        };
    }

}
