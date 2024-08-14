package org.twins.core.dao.twinflow;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionTriggerRepository extends CrudRepository<TwinflowTransitionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionTriggerEntity> {
    String CACHE_TRANSITION_TRIGGERS_BY_TRANSITION_ID_ORDERED = "TwinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder";

    @Cacheable(value = CACHE_TRANSITION_TRIGGERS_BY_TRANSITION_ID_ORDERED, key = "{#twinflowTransitionId}")
    List<TwinflowTransitionTriggerEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinFlowId);

    List<TwinflowTransitionTriggerEntity> findAllByTwinflowTransitionIdInOrderByOrder(Collection<UUID> twinflowTransitionIds);

    List<TwinflowTransitionTriggerEntity> findAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

    void deleteAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

}
