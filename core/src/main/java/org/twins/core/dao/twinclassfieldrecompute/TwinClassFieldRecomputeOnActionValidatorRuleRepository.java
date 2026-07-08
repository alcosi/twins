package org.twins.core.dao.twinclassfieldrecompute;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRecomputeOnActionValidatorRuleRepository
        extends CrudRepository<TwinClassFieldRecomputeOnActionValidatorRuleEntity, UUID>,
                JpaSpecificationExecutor<TwinClassFieldRecomputeOnActionValidatorRuleEntity> {

    String CACHE_BY_RECOMPUTE_ID_IN = "TwinClassFieldRecomputeOnActionValidatorRuleRepository.findByTwinClassFieldRecomputeOnActionIdInOrderByOrder";

    /**
     * Hot path: called by TwinClassFieldRecomputeService for the set of OnAction recompute rules
     * that survived validator filtering. Cached by the unique-key of the input collection.
     */
    @Cacheable(value = CACHE_BY_RECOMPUTE_ID_IN,
            key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinClassFieldRecomputeOnActionIds)")
    List<TwinClassFieldRecomputeOnActionValidatorRuleEntity> findByTwinClassFieldRecomputeOnActionIdInOrderByOrder(
            Collection<UUID> twinClassFieldRecomputeOnActionIds);

    List<TwinClassFieldRecomputeOnActionValidatorRuleEntity> findByTwinClassFieldRecomputeOnActionIdOrderByOrder(
            UUID twinClassFieldRecomputeOnActionId);
}
