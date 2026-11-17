package org.twins.core.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

/**
 * Carries the runtime-only {@code newRow} flag backing {@code Persistable.isNew()}: true for a
 * freshly constructed instance, flipped off at {@code @PostLoad}/{@code @PostPersist}. Needed for
 * entities whose create paths pre-assign ids before save (UuidUtils.generate, fixed system ids,
 * duplicate reserved ids), where id nullness is not a reliable insert discriminator.
 * Replaces the former {@code TwinEntity}/{@code TwinLinkEntity} createElseUpdate flag.
 *
 * Update flows that rebuild a fresh instance for an existing row (upsert with a fixed id,
 * relink) must explicitly call {@code setNewRow(false)} — see
 * {@code SystemEntityBootstrapService.saveSystemTwin} and {@code TwinLinkService.processAlreadyExisted}.
 */
@MappedSuperclass
@Data
@Accessors(chain = true)
public abstract class PersistableEntity implements Identifiable<UUID> {

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

    @JsonIgnore // same as Identifiable's default isNew — the flag must not leak into serialization
    public boolean isNew() {
        return newRow;
    }
}
