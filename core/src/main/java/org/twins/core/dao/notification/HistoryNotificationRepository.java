package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationRepository extends CrudRepository<HistoryNotificationEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationEntity> {
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdAndTwinClassFieldIdAndNotificationSchemaId(String historyTypeId, UUID twinClassId, UUID twinClassFieldId, UUID notificationSchemaId);
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdAndNotificationSchemaId(String historyTypeId, UUID twinClassId, UUID schemaId);

    @Query(value = "SELECT hn.* FROM history_notification hn " +
            "JOIN twin_class tc ON hn.twin_class_id = tc.id " +
            "LEFT JOIN twin_class tc2 ON tc2.id = :twinClassId " +
            "WHERE hn.history_type_id = :historyTypeId " +
            "AND hn.notification_schema_id = :notificationSchemaId " +
            "AND (:twinClassFieldId IS NULL OR hn.twin_class_field_id IS NULL OR hn.twin_class_field_id = :twinClassFieldId) " +
            "AND (" +
            "   hn.twin_class_id = :twinClassId " +  // direct match
            "   OR tc2.extends_hierarchy_tree @> ltree(replace(hn.twin_class_id::text, '-', '_')) " +  // notification's class is in twin's extends hierarchy
            ")",
            nativeQuery = true)
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdExtendsHierarchyAndTwinClassFieldIdAndNotificationSchemaId(
            @Param("historyTypeId") String historyTypeId,
            @Param("twinClassId") UUID twinClassId,
            @Param("twinClassFieldId") UUID twinClassFieldId,
            @Param("notificationSchemaId") UUID notificationSchemaId);

    @Query(value = "SELECT hn.* FROM history_notification hn " +
            "LEFT JOIN twin_class tc2 ON tc2.id = :twinClassId " +
            "WHERE hn.history_type_id = :historyTypeId " +
            "AND hn.notification_schema_id = :notificationSchemaId " +
            "AND hn.twin_class_field_id IS NULL " +  // only non-field-specific notifications
            "AND (" +
            "   hn.twin_class_id = :twinClassId " +  // direct match
            "   OR tc2.extends_hierarchy_tree @> ltree(replace(hn.twin_class_id::text, '-', '_')) " +  // notification's class is in twin's extends hierarchy
            ")",
            nativeQuery = true)
    List<HistoryNotificationEntity> findByHistoryTypeIdAndTwinClassIdExtendsHierarchyAndNotificationSchemaId(
            @Param("historyTypeId") String historyTypeId,
            @Param("twinClassId") UUID twinClassId,
            @Param("notificationSchemaId") UUID notificationSchemaId);
}
