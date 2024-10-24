package org.twins.core.dao.validator;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionValidatorRuleRepository extends CrudRepository<TwinflowTransitionValidatorRuleEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionValidatorRuleEntity> {
    String CACHE_TRANSITION_VALIDATOR_BY_TRANSITION_ID_ORDERED = "TwinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder";

    @Cacheable(value = CACHE_TRANSITION_VALIDATOR_BY_TRANSITION_ID_ORDERED, key = "#twinflowTransitionId")
    List<TwinflowTransitionValidatorRuleEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinflowTransitionId);

    List<TwinflowTransitionValidatorRuleEntity> findAllByTwinflowTransitionIdInOrderByOrder(Collection<UUID> twinflowTransitionIds);

    List<TwinflowTransitionValidatorRuleEntity> findAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

    void deleteAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

}
