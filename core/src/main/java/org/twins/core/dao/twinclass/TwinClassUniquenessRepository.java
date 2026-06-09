package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassUniquenessRepository extends CrudRepository<TwinClassUniquenessEntity, UUID>, JpaSpecificationExecutor<TwinClassUniquenessEntity> {
    Collection<TwinClassUniquenessEntity> findAllByTwinClassIdIn(Collection<UUID> twinClassIds);

    List<TwinClassUniquenessEntity> findByTwinClassId(UUID twinClassId);
}
