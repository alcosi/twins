package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionTriggerRepository extends CrudRepository<TwinflowTransitionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionTriggerEntity> {
    List<TwinflowTransitionTriggerEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinFlowId);
}
