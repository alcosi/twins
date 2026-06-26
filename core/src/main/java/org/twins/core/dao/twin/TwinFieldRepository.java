package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TwinFieldRepository<E extends TwinFieldBaseEntity> extends CrudRepository<E, UUID>, JpaSpecificationExecutor<E> {
    List<E> findByTwinIdIn(Collection<UUID> twinIds);

    boolean existsByTwinClassFieldId(UUID twinClassFieldId);

    List<UUID> findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(UUID twinClassId, Collection<UUID> twinClassFields);

    void deleteByTwinSpecOnly_TwinClassIdAndTwinClassFieldIdIn(UUID twinClassId, Collection<UUID> twinClassFields);

    void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId);

    void deleteByTwinIdAndTwinClassFieldIdIn(UUID twinId, Set<UUID> twinClassFieldIds);
}
