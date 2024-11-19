package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinStatusRepository extends CrudRepository<TwinStatusEntity, UUID>, JpaSpecificationExecutor<TwinStatusEntity> {
    List<TwinStatusEntity> findByTwinClassId(UUID twinClassId);

    List<TwinStatusEntity> findByTwinClassIdIn(Set<UUID> twinClassIdList);

    List<TwinStatusEntity> findByIdIn(Collection<UUID> statusIds);
}
