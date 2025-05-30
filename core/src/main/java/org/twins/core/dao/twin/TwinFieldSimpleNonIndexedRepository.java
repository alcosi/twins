package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TwinFieldSimpleNonIndexedRepository extends CrudRepository<TwinFieldSimpleNonIndexedEntity , UUID>, JpaSpecificationExecutor<TwinFieldSimpleNonIndexedEntity> {

    List<TwinFieldSimpleNonIndexedEntity> findByTwinId(UUID twinId);
}
