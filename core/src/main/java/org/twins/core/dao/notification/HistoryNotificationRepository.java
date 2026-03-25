package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationRepository extends CrudRepository<HistoryNotificationEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationEntity> {
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdInAndNotificationSchemaId(String historyTypeId, Collection<UUID> twinClassId, UUID notificationSchemaId);
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdInAndTwinClassFieldIdAndNotificationSchemaId(String historyTypeId, Collection<UUID> twinClassId, UUID twinClassFieldId, UUID notificationSchemaId);
}
