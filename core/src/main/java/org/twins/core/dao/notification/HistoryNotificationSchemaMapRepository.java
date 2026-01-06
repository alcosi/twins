package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationSchemaMapRepository extends CrudRepository<HistoryNotificationSchemaMapEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationSchemaMapEntity> {
    List<HistoryNotificationSchemaMapEntity> findByHistoryTypeIdAndTwinClassIdAndTwinClassFieldIdAndNotificationSchemaId(String historyTypeId, UUID twinClassId, UUID twinClassFieldId, UUID notificationSchemaId);
    List<HistoryNotificationSchemaMapEntity> findByHistoryTypeIdAndTwinClassIdAndNotificationSchemaId(String historyTypeId, UUID twinClassId, UUID schemaId);
}

