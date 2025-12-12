package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_business_account")
public class DomainBusinessAccountEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "tier_id")
    private UUID tierId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "attachments_storage_used_count")
    private Long attachmentsStorageUsedCount;

    @Column(name = "attachments_storage_used_size")
    private Long attachmentsStorageUsedSize;

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountEntity businessAccount;

    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "tier_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TierEntity tier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_schema_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private NotificationSchemaEntity notificationSchema;

//    @ManyToOne
//    @JoinColumn(name = "twinflow_schema_id", insertable = false, updatable = false)
//    private TwinflowSchemaEntity twinflowSchema;

//    @ManyToOne
//    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false)
//    private TwinClassSchemaEntity twinClassSchema;

    public String easyLog(Level level) {
        return "domainBusinessAccount[id:" + id + ", domainId:" + domainId + ", businessAccountId:" + businessAccountId+ "]";
    }
}
