package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.EventType;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.datascope.DataScopeType;
import org.twins.core.enums.domain.DomainOperationStatus;
import org.twins.core.enums.domain.DomainVersionAuditOperation;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

/**
 * Full change history of the versioned configuration tables, one row per row-change event
 * (TWINS-719). Written by the versioning triggers (all paths), never by Hibernate. Rows are
 * immutable; {@code domain_version_id} intentionally carries no FK — windows are collapsed and
 * deleted on release/import, while the audit trail survives.
 */
@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "domain_version_audit")
@DataScope(DataScopeType.DOMAIN)
public class DomainVersionAuditEntity implements EasyLoggable, Identifiable<UUID> {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    // window stamp; intentionally no FK: windows are collapsed/deleted
    @Column(name = "domain_version_id", nullable = false)
    private UUID domainVersionId;

    // DB-generated (bigserial): strict insertion order — uuid v7 has only millisecond precision,
    // records within one millisecond (bulk writes!) order randomly
    @Generated(event = EventType.INSERT)
    @Column(name = "seq", insertable = false, updatable = false, nullable = false)
    private Long seq;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    // PK of the changed row (every versioned table has a uuid id)
    @Column(name = "row_id", nullable = false)
    private UUID rowId;

    @Column(name = "operation", nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainVersionAuditOperation operation;

    @Column(name = "domain_operation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainOperationStatus domainOperationStatus;

    // the row's stamp BEFORE the operation: stored images are cleaned of the versioning columns,
    // so the prior stamp (needed by resurrection and rollback) lives in this dedicated column;
    // NULL for INSERT
    @Column(name = "before_domain_version_id")
    private UUID beforeDomainVersionId;

    // to_jsonb(OLD) cleaned of versioning/derived columns; NULL only for INSERT
    @Type(JsonType.class)
    @Column(name = "before", columnDefinition = "jsonb")
    private Map<String, Object> before;

    // to_jsonb(NEW) cleaned likewise; NULL for DELETE and physical removal
    @Type(JsonType.class)
    @Column(name = "after", columnDefinition = "jsonb")
    private Map<String, Object> after;

    @Column(name = "changed_at", nullable = false)
    private Timestamp changedAt;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    private DomainVersionEntity domainVersionSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity actorUser;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false)
    private UserEntity actorUserSpecOnly;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "domainVersionAudit[" + id + "]";
            default:
                return "domainVersionAudit[id:" + id + ", table:" + tableName + ", rowId:" + rowId + ", operation:" + operation + "]";
        }
    }
}
