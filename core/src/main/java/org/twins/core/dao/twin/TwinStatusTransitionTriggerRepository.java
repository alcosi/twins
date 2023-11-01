package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinStatusTransitionTriggerRepository extends CrudRepository<TwinStatusTransitionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinStatusTransitionTriggerEntity> {
    List<TwinStatusTransitionTriggerEntity> findAllByTwinStatusIdAndTypeAndActiveOrderByOrder(UUID twinStatusId, TwinStatusTransitionTriggerEntity.TransitionType type, boolean active);
}
