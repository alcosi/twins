package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public abstract class TwinFieldStorage {
    abstract void load(Kit<TwinEntity, UUID> twinsKit);

    abstract boolean hasStrictValues(UUID twinClassFieldId);

    abstract boolean isLoaded(TwinEntity twinEntity);

    abstract void initEmpty(TwinEntity twinEntity);

    abstract Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet);

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || canBeMerged(obj);
    }

    /**
     * This method helps to merge storages from different twin class fields for load data.
     * For example, we have class with 10 fields. They all are simple, so we can load them all at once by on storage
     * @param o
     * @return
     */
    abstract boolean canBeMerged(Object o);

    protected boolean isSameClass(Object o) {
        return o != null && o.getClass() == this.getClass();
    }
}
