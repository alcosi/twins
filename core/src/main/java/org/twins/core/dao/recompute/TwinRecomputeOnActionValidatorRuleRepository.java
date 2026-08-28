package org.twins.core.dao.recompute;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinRecomputeOnActionValidatorRuleRepository
        extends CrudRepository<TwinRecomputeOnActionValidatorRuleEntity, UUID>,
                JpaSpecificationExecutor<TwinRecomputeOnActionValidatorRuleEntity> {

    String CACHE_BY_RECOMPUTE_ID_IN = "TwinRecomputeOnActionValidatorRuleRepository.findByTwinRecomputeOnActionIdInOrderByOrder";

    /**
     * Hot path: called by TwinRecomputeService for the set of OnAction recompute rules that survived
     * validator filtering. Cached by the unique-key of the input collection.
     */
    @Cacheable(value = CACHE_BY_RECOMPUTE_ID_IN,
            key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#twinRecomputeOnActionIds)")
    List<TwinRecomputeOnActionValidatorRuleEntity> findByTwinRecomputeOnActionIdInOrderByOrder(
            Collection<UUID> twinRecomputeOnActionIds);

    List<TwinRecomputeOnActionValidatorRuleEntity> findByTwinRecomputeOnActionIdOrderByOrder(
            UUID twinRecomputeOnActionId);
}
