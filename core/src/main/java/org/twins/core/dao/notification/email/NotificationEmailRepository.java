package org.twins.core.dao.notification.email;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationEmailRepository extends CrudRepository<NotificationEmailEntity, UUID>, JpaSpecificationExecutor<NotificationEmailEntity> {
    <T> T findById(UUID id, Class<T> type);

    @Query(value = "from NotificationEmailEntity where (domainId = :domainId or domainId is null) and eventId = :eventId order by domainId limit 1")
    NotificationEmailEntity findByDomainIdAndEventId(UUID domainId, UUID eventId);
}
