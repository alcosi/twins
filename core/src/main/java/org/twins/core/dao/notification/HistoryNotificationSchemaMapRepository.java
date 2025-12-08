package org.twins.core.dao.notification;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HistoryNotificationSchemaMapRepository extends CrudRepository<HistoryNotificationSchemaMapEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationSchemaMapEntity> {
    @Override
    HistoryNotificationSchemaMapEntity findById(UUID uuid);
}

