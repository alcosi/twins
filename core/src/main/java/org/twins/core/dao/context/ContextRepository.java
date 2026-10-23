package org.twins.core.dao.context;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContextRepository extends CrudRepository<ContextEntity, UUID>, JpaSpecificationExecutor<ContextEntity> {
    <T> T findById(UUID id, Class<T> type);
}
