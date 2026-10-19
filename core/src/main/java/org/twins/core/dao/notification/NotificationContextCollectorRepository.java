package org.twins.core.dao.notification;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface NotificationContextCollectorRepository extends CrudRepository<NotificationContextCollectorEntity, UUID>, JpaSpecificationExecutor<NotificationContextCollectorEntity> {

    String CACHE_NOTIFICATION_CONTEXT_COLLECTORS_BY_CONTEXT_ID = "NotificationContextCollectorRepository.findAllByNotificationContextId";

    @Cacheable(value = CACHE_NOTIFICATION_CONTEXT_COLLECTORS_BY_CONTEXT_ID, key = "#contextId")
    List<NotificationContextCollectorEntity> findAllByNotificationContextId(UUID contextId);

    List<NotificationContextCollectorEntity> findAllByNotificationContextIdIn(Set<UUID> contextIds);
}
