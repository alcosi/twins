package org.twins.core.dao.twinflow;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionValidatorRepository extends CrudRepository<TwinflowTransitionValidatorEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionValidatorEntity> {
    String CACHE_TRANSITION_VALIDATOR_BY_TRANSITION_ID_ORDERED = "TwinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder";

    @Cacheable(value = CACHE_TRANSITION_VALIDATOR_BY_TRANSITION_ID_ORDERED, key = "{#twinflowTransitionId}")
    List<TwinflowTransitionValidatorEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinflowTransitionId);

    List<TwinflowTransitionValidatorEntity> findAllByTwinflowTransitionIdInOrderByOrder(Collection<UUID> twinflowTransitionIds);

    List<TwinflowTransitionValidatorEntity> findAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

    void deleteAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

}
