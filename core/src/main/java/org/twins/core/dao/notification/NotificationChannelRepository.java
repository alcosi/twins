package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationChannelRepository extends CrudRepository<NotificationChannelEntity, UUID>, JpaSpecificationExecutor<NotificationChannelEntity> {
    <T> T findById(UUID id, Class<T> type);
}

