package org.twins.core.dao.trigger;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryTriggerRepository extends CrudRepository<TwinFactoryTriggerEntity, UUID>, JpaSpecificationExecutor<TwinFactoryTriggerEntity> {
    List<TwinFactoryTriggerEntity> findByTwinFactoryId(UUID twinFactoryId);
}
