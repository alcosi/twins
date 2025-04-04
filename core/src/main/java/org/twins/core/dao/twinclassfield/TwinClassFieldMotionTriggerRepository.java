package org.twins.core.dao.twinclassfield;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldMotionTriggerRepository extends CrudRepository<TwinClassFieldMotionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldMotionTriggerEntity> {
    String CACHE_TRANSITION_TRIGGERS_BY_TRANSITION_ID_ORDERED = "TwinflowTransitionTriggerRepository.findByTwinflowTransitionIdOrderByOrder";

    @Cacheable(value = CACHE_TRANSITION_TRIGGERS_BY_TRANSITION_ID_ORDERED, key = "#twinflowTransitionId")
    List<TwinClassFieldMotionTriggerEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinflowTransitionId);

    List<TwinClassFieldMotionTriggerEntity> findAllByTwinflowTransitionIdInOrderByOrder(Collection<UUID> twinflowTransitionIds);

    List<TwinClassFieldMotionTriggerEntity> findAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

    void deleteAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

}
