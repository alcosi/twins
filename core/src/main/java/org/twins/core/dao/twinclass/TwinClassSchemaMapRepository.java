package org.twins.core.dao.twinclass;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TwinClassSchemaMapRepository extends CrudRepository<TwinClassSchemaMapEntity, UUID> {
    Optional<TwinClassSchemaMapEntity> findByTwinClassSchemaIdAndTwinClassId(UUID twinClassSchemaId, UUID twinClassId);
}
