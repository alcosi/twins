package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationChannelEventRepository extends CrudRepository<NotificationChannelEventEntity, UUID>, JpaSpecificationExecutor<NotificationChannelEventEntity> {
    NotificationChannelEventEntity findByEventCode(String eventId);
}
