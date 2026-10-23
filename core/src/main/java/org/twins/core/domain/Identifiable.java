package org.twins.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Persistable;

/**
 * Interface for entities used in EntitiesChangesCollector and for everything postponable into
 * MapperContext related objects: the generic parameter is the id type (UUID for regular entities,
 * Integer/String for featurer/historyType and friends). Transport value objects with composite
 * identity (e.g. FeaturerParams) can not implement it because of setId.
 *
 * Extends {@link Persistable} with the default id-null insert detection — the same semantics
 * Spring Data applies to non-Persistable entities, so repository save() behavior is unchanged
 * for existing implementors. Implementors that pre-assign ids before save MUST override
 * {@link #isNew()} with a real insert flag (see DomainConfig contract).
 */
public interface Identifiable<ID> extends Persistable<ID> {
    ID getId();

    // for Accessor(chain = true)
    Identifiable<ID> setId(ID id);

    @JsonIgnore
    @Override
    default boolean isNew() {
        return getId() == null;
    }
}
