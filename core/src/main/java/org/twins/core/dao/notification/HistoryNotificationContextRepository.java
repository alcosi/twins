package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryNotificationContextRepository extends CrudRepository<HistoryNotificationContextEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationContextEntity> {
    <T> T findById(UUID id, Class<T> type);
}

