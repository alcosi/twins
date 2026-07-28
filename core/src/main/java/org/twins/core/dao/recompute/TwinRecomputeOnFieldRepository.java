package org.twins.core.dao.recompute;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinRecomputeOnFieldRepository
        extends CrudRepository<TwinRecomputeOnFieldEntity, UUID>,
                JpaSpecificationExecutor<TwinRecomputeOnFieldEntity> {

    String CACHE_BY_PUBLISHER_FIELD_IN = "TwinRecomputeOnFieldRepository.findByPublisherTwinClassFieldIdIn";

    /**
     * Hot path: called by {@code TwinRecomputeService} for the set of publisher fields touched in a tx.
     * Cached by the unique-key of the input collection so identical snapshots hit the cache.
     */
    @Cacheable(value = CACHE_BY_PUBLISHER_FIELD_IN,
            key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#publisherFieldIds)")
    List<TwinRecomputeOnFieldEntity> findByPublisherTwinClassFieldIdIn(Collection<UUID> publisherFieldIds);
}
