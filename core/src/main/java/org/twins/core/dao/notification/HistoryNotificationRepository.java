package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.history.HistoryType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationRepository extends CrudRepository<HistoryNotificationEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationEntity> {
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdInAndNotificationSchemaIdAndActiveTrue(HistoryType historyTypeId, Collection<UUID> twinClassId, UUID notificationSchemaId);
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdInAndTwinClassFieldIdAndNotificationSchemaIdAndActiveTrue(HistoryType historyTypeId, Collection<UUID> twinClassId, UUID twinClassFieldId, UUID notificationSchemaId);
    // Bulk replacement for the two methods above: one query over the union of keys across a chunk of tasks.
    // twinClassFieldId is intentionally NOT in the query — matched in-memory (see HistoryNotificationService.findConfigsForTasks)
    // to preserve the original null-semantics (fieldId == null → accept any; else exact match).
    List<HistoryNotificationEntity> findByHistoryTypeIdInAndTwinClassIdInAndNotificationSchemaIdInAndActiveTrue(
            Collection<HistoryType> historyTypeIds,
            Collection<UUID> twinClassIds,
            Collection<UUID> notificationSchemaIds);
}
