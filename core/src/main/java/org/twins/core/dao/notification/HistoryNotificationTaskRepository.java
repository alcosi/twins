package org.twins.core.dao.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationTaskRepository extends CrudRepository<HistoryNotificationTaskEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationTaskEntity> {
    List<HistoryNotificationTaskEntity> findByStatusIdIn(Collection<HistoryNotificationTaskStatus> statusIds);
    List<HistoryNotificationTaskEntity> findByStatusIdIn(Collection<HistoryNotificationTaskStatus> statusIds, Pageable pageable);
}
