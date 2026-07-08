package org.twins.core.dao.twinclassfieldrecompute;

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

    List<TwinClassFieldRecomputeOnFieldEntity> findByPublisherTwinClassFieldIdIn(Collection<UUID> publisherFieldIds);

    List<TwinClassFieldRecomputeOnFieldEntity> findByDomainId(UUID domainId);

    List<TwinClassFieldRecomputeOnFieldEntity> findBySubscriberTwinClassFieldId(UUID subscriberFieldId);

    void deleteBySubscriberTwinClassFieldId(UUID subscriberFieldId);
}
