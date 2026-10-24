package org.twins.core.dao;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.DomainConfig;

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
