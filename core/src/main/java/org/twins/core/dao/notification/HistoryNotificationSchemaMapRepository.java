package org.twins.core.dao.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface HistoryNotificationSchemaMapRepository extends CrudRepository<HistoryNotificationSchemaMapEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationSchemaMapEntity> {
    Set<HistoryNotificationSchemaMapEntity> findByNotificationSchemaIdAndNotificationChannelEvent_EventCodeIn(UUID schemaId, Collection<String> eventCodes);
}
