package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationContextCollectorRepository extends CrudRepository<NotificationContextCollectorEntity, UUID>, JpaSpecificationExecutor<NotificationContextCollectorEntity> {

    List<NotificationContextCollectorEntity> findAllByNotificationContextId(UUID contextId);
}

