package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinflowSchemaMapRepository extends CrudRepository<TwinflowSchemaMapEntity, UUID>, JpaSpecificationExecutor<TwinflowSchemaMapEntity> {
    TwinflowSchemaMapEntity findByTwinflowSchemaIdAndTwinClassId(UUID twinflowSchemaId, UUID twinClassId);
    List<TwinflowSchemaMapEntity> findByTwinflowSchemaIdAndTwinClassIdIn(UUID twinflowSchemaId, Set<UUID> twinClassId);
}
