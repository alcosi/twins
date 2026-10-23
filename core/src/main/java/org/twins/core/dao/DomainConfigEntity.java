package org.twins.core.dao;

import jakarta.persistence.MappedSuperclass;
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
public abstract class DomainConfigEntity extends PersistableEntity implements DomainConfig {
}
