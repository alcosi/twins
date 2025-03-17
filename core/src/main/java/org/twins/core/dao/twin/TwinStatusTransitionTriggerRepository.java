package org.twins.core.dao.twin;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinStatusTransitionTriggerRepository extends CrudRepository<TwinStatusTransitionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinStatusTransitionTriggerEntity> {

    String CACHE_TWIN_STATUS_TRANSITION_TRIGGER = "TwinStatusTransitionTriggerRepository.findAllByTwinStatusIdAndTypeAndActiveOrderByOrder";
    @Cacheable(value = CACHE_TWIN_STATUS_TRANSITION_TRIGGER, key = "#twinStatusId + '' + #type + '' + #active")
    List<TwinStatusTransitionTriggerEntity> findAllByTwinStatusIdAndTypeAndActiveOrderByOrder(UUID twinStatusId, TwinStatusTransitionTriggerEntity.TransitionType type, boolean active);
}
