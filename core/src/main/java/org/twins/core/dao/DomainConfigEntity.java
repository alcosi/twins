package org.twins.core.dao;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.DomainConfig;

import java.util.UUID;

/**
 * Base for versioned domain config entities (TWINS-719 domain versioning): {@link DomainConfig}
 * contract plus the {@code newRow} insert/update discriminator inherited from
 * {@link PersistableEntity} — create vs update is decided by a real flag, not by id nullness
 * (create paths may pre-assign ids before save).
 *
 * An entity that needs its own superclass slot can instead implement {@link DomainConfig}
 * directly and repeat the {@link PersistableEntity} boilerplate.
 */
@MappedSuperclass
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
public abstract class DomainConfigEntity extends PersistableEntity implements DomainConfig {

    // Field-name constants for specifications, reachable through any subclass (e.g.
    // TwinClassEntity.DOMAIN_ID): each subclass's Lombok @FieldNameConstants generates its own
    // Fields holder which HIDES this class's Fields, and camelCase constant names are impossible
    // here — a constant would clash with the instance field of the same name declared below.
    public static final String DOMAIN_ID = "domainId";
    public static final String DOMAIN_VERSION_ID = "domainVersionId";

    @Column(name = "domain_id")
    private UUID domainId;

    // DB-maintained by the versioning triggers (TWINS-719) — Hibernate never writes it
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "domain_version_id", insertable = false, updatable = false)
    private UUID domainVersionId;

    // the version window that last touched this row (loaded on demand, see load_method_pattern.md)
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
}
