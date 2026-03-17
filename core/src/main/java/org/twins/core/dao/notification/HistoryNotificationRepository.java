package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationRepository extends CrudRepository<HistoryNotificationEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationEntity> {
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdAndTwinClassFieldIdAndNotificationSchemaId(String historyTypeId, UUID twinClassId, UUID twinClassFieldId, UUID notificationSchemaId);
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdAndNotificationSchemaId(String historyTypeId, UUID twinClassId, UUID schemaId);
}
