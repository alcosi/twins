package org.twins.core.dao.twinclassfieldrecompute;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRecomputeOnFieldRepository
        extends CrudRepository<TwinClassFieldRecomputeOnFieldEntity, UUID>,
                JpaSpecificationExecutor<TwinClassFieldRecomputeOnFieldEntity> {

    String CACHE_BY_PUBLISHER_FIELD_IN = "TwinClassFieldRecomputeOnFieldRepository.findByPublisherTwinClassFieldIdIn";

    /**
     * Hot path: called by TwinClassFieldRecomputeService for the set of publisher fields touched in a tx.
     * Cached by the unique-key of the input collection so identical snapshots hit the cache.
     */
    @Cacheable(value = CACHE_BY_PUBLISHER_FIELD_IN,
            key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#publisherFieldIds)")
    List<TwinClassFieldRecomputeOnFieldEntity> findByPublisherTwinClassFieldIdIn(Collection<UUID> publisherFieldIds);

    List<TwinClassFieldRecomputeOnFieldEntity> findByDomainId(UUID domainId);

    List<TwinClassFieldRecomputeOnFieldEntity> findBySubscriberTwinClassFieldId(UUID subscriberFieldId);

    void deleteBySubscriberTwinClassFieldId(UUID subscriberFieldId);
}
