package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TwinStatusTransitionTriggerTaskRepository extends CrudRepository<TwinStatusTransitionTriggerTaskEntity, UUID>, JpaSpecificationExecutor<TwinStatusTransitionTriggerTaskEntity> {
    List<TwinStatusTransitionTriggerTaskEntity> findByStatusIdIn(List<TwinStatusTransitionTriggerStatus> needStartStatuses);

}
