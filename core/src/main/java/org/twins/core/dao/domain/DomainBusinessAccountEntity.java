package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_business_account")
public class DomainBusinessAccountEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

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

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @Column(name = "tier_id")
    private UUID tierId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "attachments_storage_used_count")
    private Long attachmentsStorageUsedCount;

    @Column(name = "attachments_storage_used_size")
    private Long attachmentsStorageUsedSize;

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

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private NotificationSchemaEntity notificationSchema;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowSchemaEntity twinflowSchema;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassSchemaEntity twinClassSchema;

    @Transient
    private Long twinsCount;

    @Transient
    private Long usersCount;

    public String easyLog(Level level) {
        return "domainBusinessAccount[id:" + id + ", domainId:" + domainId + ", businessAccountId:" + businessAccountId + "]";
    }

}
