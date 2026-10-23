package org.twins.core.dao;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.domain.DomainConfig;

/**
 * Base for versioned domain config entities (TWINS-719 domain versioning): carries the
 * runtime-only {@code newRow} flag implementing {@code Persistable.isNew} (via
 * {@link DomainConfig}) — flipped off at {@code @PostLoad}/{@code @PostPersist}, so create vs
 * update is decided by a real insert discriminator, not by id nullness (create paths may
 * pre-assign ids before save).
 *
 * An entity that needs its own superclass slot can instead implement {@link DomainConfig}
 * directly and repeat this boilerplate.
 */
@MappedSuperclass
@Data
@Accessors(chain = true)
public abstract class DomainConfigEntity implements DomainConfig {

    // insert/update discriminator for Persistable.isNew (TWINS-719) - runtime-only flag, not a column
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private boolean newRow = true;

    @PostLoad
    @PostPersist
    protected void markPersisted() {
        newRow = false;
    }

    @Override
    public boolean isNew() {
        return newRow;
    }
}
