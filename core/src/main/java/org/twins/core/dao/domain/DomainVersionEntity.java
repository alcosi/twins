package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.PersistableEntity;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.enums.datascope.DataScopeType;
import org.twins.core.enums.domain.DomainVersionStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * One row per version window / release / import (TWINS-719 domain versioning).
 * The OPEN row is the mutable draft window collecting configuration changes (version = 'draft');
 * at release/import it is frozen to RELEASED with the final label, a monotonic per-domain
 * {@code order} and the snapshot/delta files. Rows are normally written by the versioning DB
 * functions/triggers; Hibernate must never touch other tables' stamps, but this table itself is
 * Java-writable (import/release services).
 */
@Data
@Entity
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
@Table(name = "domain_version")
@DataScope(DataScopeType.DOMAIN)
public class DomainVersionEntity extends PersistableEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    // OPEN window: 'draft'; release: final label; import: '(import) 1.2'
    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DomainVersionStatus status;

    // monotonic per-domain counter, assigned at release/import; NULL for the open window
    @Column(name = "order")
    private Long order;

    // full snapshot of the configuration state — a resource (snapshots can be large)
    @Column(name = "full_snapshot_resource_id")
    private UUID fullSnapshotResourceId;

    // SHA-256 of the full snapshot (always set for RELEASED)
    @Column(name = "full_snapshot_hash")
    private String fullSnapshotHash;

    // forward delta: prev release -> this release (cross-env import) — a resource
    @Column(name = "delta_resource_id")
    private UUID deltaResourceId;

    @Column(name = "delta_hash")
    private String deltaHash;

    // reverse delta: this release -> prev release (local restore) — a resource
    @Column(name = "rollback_delta_resource_id")
    private UUID rollbackDeltaResourceId;

    @Column(name = "rollback_delta_hash")
    private String rollbackDeltaHash;

    @Column(name = "previous_domain_version_id")
    private UUID previousDomainVersionId;

    @Column(name = "started_at", nullable = false)
    private Timestamp startedAt;

    @Column(name = "start_duration")
    private Long startDuration;

    @Column(name = "released_at")
    private Timestamp releasedAt;

    @Column(name = "release_duration")
    private Long releaseDuration;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_domain_version_id", insertable = false, updatable = false)
    private DomainVersionEntity previousDomainVersion;

    // payload resources (snapshot/deltas) — transient, batch-loaded via DomainVersionService.loadResources
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity fullSnapshotResource;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity deltaResource;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity rollbackDeltaResource;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
        if (status == null)
            status = DomainVersionStatus.OPEN;
        if (startedAt == null)
            startedAt = Timestamp.from(Instant.now());
    }

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "domainVersion[" + id + "]";
            default:
                return "domainVersion[id:" + id + ", domainId:" + domainId + ", version:" + version + ", status:" + status + "]";
        }
    }
}
