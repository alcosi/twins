package org.twins.core.dao.twinclassfield;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.action.TwinAction;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRecomputeOnActionRepository
        extends CrudRepository<TwinClassFieldRecomputeOnActionEntity, UUID>,
                JpaSpecificationExecutor<TwinClassFieldRecomputeOnActionEntity> {

    String CACHE_BY_PUBLISHER_CLASS_ACTION = "TwinClassFieldRecomputeOnActionRepository.findByPublisherTwinClassIdAndPublisherTwinAction";
    String CACHE_BY_PUBLISHER_CLASS_IN = "TwinClassFieldRecomputeOnActionRepository.findByPublisherTwinClassIdIn";

    /**
     * Hot path: called by TwinClassFieldRecomputeService once per (publisher twin class, action) pair
     * observed in a tx. Cache key is the (classId, action.name()) pair — small cardinality, stable.
     */
    @Cacheable(value = CACHE_BY_PUBLISHER_CLASS_ACTION,
            key = "#publisherTwinClassId + '' + #action.name()")
    List<TwinClassFieldRecomputeOnActionEntity> findByPublisherTwinClassIdAndPublisherTwinAction(
            UUID publisherTwinClassId, TwinAction action);

    /**
     * Hot path: called by {@code TwinClassService.loadRecomputeOnAction(Collection<TwinClassEntity>)} to
     * populate {@link TwinClassEntity#getRecomputeOnAction()} kits in one batch.
     */
    @Cacheable(value = CACHE_BY_PUBLISHER_CLASS_IN,
            key = "T(org.cambium.common.util.CollectionUtils).generateUniqueKey(#publisherTwinClassIds)")
    List<TwinClassFieldRecomputeOnActionEntity> findByPublisherTwinClassIdIn(
            Collection<UUID> publisherTwinClassIds);

    List<TwinClassFieldRecomputeOnActionEntity> findByPublisherTwinClassIdInAndPublisherTwinAction(
            Collection<UUID> publisherTwinClassIds, TwinAction action);

    List<TwinClassFieldRecomputeOnActionEntity> findByDomainId(UUID domainId);

    List<TwinClassFieldRecomputeOnActionEntity> findBySubscriberTwinClassFieldId(UUID subscriberFieldId);

    void deleteBySubscriberTwinClassFieldId(UUID subscriberFieldId);
}
