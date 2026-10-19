package org.twins.core.dao.recompute;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinRecomputeSubscriberRepository
        extends CrudRepository<TwinRecomputeSubscriberEntity, UUID>,
                JpaSpecificationExecutor<TwinRecomputeSubscriberEntity> {

    List<TwinRecomputeSubscriberEntity> findByDomainId(UUID domainId);

    List<TwinRecomputeSubscriberEntity> findBySubscriberTwinClassFieldId(UUID subscriberTwinClassFieldId);

    void deleteBySubscriberTwinClassFieldId(UUID subscriberTwinClassFieldId);
}
