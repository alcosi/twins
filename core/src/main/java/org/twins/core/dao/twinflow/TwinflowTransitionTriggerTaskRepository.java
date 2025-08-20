package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionTriggerTaskRepository extends CrudRepository<TwinflowTransitionTriggerTaskEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionTriggerTaskEntity> {
    List<TwinflowTransitionTriggerTaskEntity> findByStatusIdIn(List<TwinflowTransitionTriggerStatus> needStartStatuses);

}
