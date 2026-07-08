package org.twins.core.dao.twinclassfieldrecompute;

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

    List<TwinClassFieldRecomputeOnActionEntity> findByPublisherTwinClassIdAndPublisherTwinAction(
            UUID publisherTwinClassId, TwinAction action);

    List<TwinClassFieldRecomputeOnActionEntity> findByPublisherTwinClassIdInAndPublisherTwinAction(
            Collection<UUID> publisherTwinClassIds, TwinAction action);

    List<TwinClassFieldRecomputeOnActionEntity> findByDomainId(UUID domainId);

    List<TwinClassFieldRecomputeOnActionEntity> findBySubscriberTwinClassFieldId(UUID subscriberFieldId);

    void deleteBySubscriberTwinClassFieldId(UUID subscriberFieldId);
}
