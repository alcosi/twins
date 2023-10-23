package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.PrettyLoggable;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "domain_business_account")
public class DomainBusinessAccountEntity implements PrettyLoggable {
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

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "twinflow_schema_id", insertable = false, updatable = false)
    private TwinflowSchemaEntity twinflowSchema;

    @ManyToOne
    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false)
    private TwinClassSchemaEntity twinClassSchema;

    public String logShort() {
        return "domainBusinessAccount[id:" + id + ", domainId:" + domainId + ", businessAccountId:" + businessAccountId+ "]";
    }
}
