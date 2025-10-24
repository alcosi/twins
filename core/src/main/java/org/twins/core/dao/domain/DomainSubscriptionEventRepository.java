package org.twins.core.dao.domain;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DomainSubscriptionEventRepository extends CrudRepository<DomainSubscriptionEventEntity, UUID>, JpaSpecificationExecutor<DomainSubscriptionEventEntity> {

    Collection<DomainSubscriptionEventEntity> findAllByDomainIdInAndSubscriptionEventTypeId(Set<UUID> domainIds, SubscriptionEventType type);

    boolean existsByDomainIdAndSubscriptionEventTypeId(UUID domainId, SubscriptionEventType type);
}
