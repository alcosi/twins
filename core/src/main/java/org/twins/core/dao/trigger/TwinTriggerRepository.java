package org.twins.core.dao.trigger;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinTriggerRepository extends CrudRepository<TwinTriggerEntity, UUID>, JpaSpecificationExecutor<TwinTriggerEntity> {
}
