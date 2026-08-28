package org.twins.core.dao.recompute;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.action.TwinAction;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinRecomputeOnActionRepository
        extends CrudRepository<TwinRecomputeOnActionEntity, UUID>,
                JpaSpecificationExecutor<TwinRecomputeOnActionEntity> {

    String CACHE_BY_PUBLISHER_CLASS_ACTION = "TwinRecomputeOnActionRepository.findByPublisherTwinClassIdAndPublisherTwinAction";
    String CACHE_BY_PUBLISHER_CLASS_IN = "TwinRecomputeOnActionRepository.findByPublisherTwinClassIdIn";

    /**
     * Hot path: called once per (publisher twin class, action) pair observed in a tx.
     * Cache key is the (classId, action.name()) pair — small cardinality, stable.
     */
    @Cacheable(value = CACHE_BY_PUBLISHER_CLASS_ACTION,
            key = "#publisherTwinClassId + '' + #action.name()")
    List<TwinRecomputeOnActionEntity> findByPublisherTwinClassIdAndPublisherTwinAction(
            UUID publisherTwinClassId, TwinAction action);

    /**
     * Hot path: called by {@code TwinRecomputeService} to load all OnAction rules for a set of publisher
     * classes in one batch.
     */
    @Cacheable(value = CACHE_BY_PUBLISHER_CLASS_IN,
            key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#publisherTwinClassIds)")
    List<TwinRecomputeOnActionEntity> findByPublisherTwinClassIdIn(Collection<UUID> publisherTwinClassIds);
}
