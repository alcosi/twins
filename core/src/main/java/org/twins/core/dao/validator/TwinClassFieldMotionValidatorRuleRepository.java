package org.twins.core.dao.validator;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldMotionValidatorRuleRepository extends CrudRepository<TwinClassFieldMotionValidatorRuleEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldMotionValidatorRuleEntity> {
    String CACHE_TRANSITION_VALIDATOR_RULES_BY_TRANSITION_ID_ORDERED = "TwinflowTransitionValidatorRepository.findByTwinflowTransitionIdOrderByOrder";

    @Cacheable(value = CACHE_TRANSITION_VALIDATOR_RULES_BY_TRANSITION_ID_ORDERED, key = "#twinflowTransitionId")
    List<TwinClassFieldMotionValidatorRuleEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinflowTransitionId);

    List<TwinClassFieldMotionValidatorRuleEntity> findAllByTwinflowTransitionIdInOrderByOrder(Collection<UUID> twinflowTransitionIds);

    List<TwinClassFieldMotionValidatorRuleEntity> findAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

    void deleteAllByTwinflowTransitionIdAndIdIn(UUID transitionId, List<UUID> idList);

}
