package org.twins.core.dao.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.HistoryNotificationTaskStatus;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryNotificationTaskRepository extends CrudRepository<HistoryNotificationTaskEntity, UUID>, JpaSpecificationExecutor<HistoryNotificationTaskEntity> {
    List<HistoryNotificationTaskEntity> findByStatusIdIn(Collection<HistoryNotificationTaskStatus> statusIds);
    List<HistoryNotificationTaskEntity> findByStatusIdIn(Collection<HistoryNotificationTaskStatus> statusIds, Pageable pageable);

    /**
     * Bulk status update — replaces saveAll/merge for detached task entities (SELECT + UPDATE per row).
     * Callers keep the in-memory entities in sync themselves; see HistoryNotificationTaskService.updateStatuses.
     */
    @Modifying
    @Query("update HistoryNotificationTaskEntity t set t.statusId = :statusId, t.statusDetails = :statusDetails, t.doneAt = :doneAt, t.attemptCount = :attemptCount where t.id in :ids")
    int updateStatusByIdIn(@Param("ids") Collection<UUID> ids,
                           @Param("statusId") HistoryNotificationTaskStatus statusId,
                           @Param("statusDetails") String statusDetails,
                           @Param("doneAt") Timestamp doneAt,
                           @Param("attemptCount") int attemptCount);

    void deleteAllByStatusIdIn(List<HistoryNotificationTaskStatus> statuses);
    long countAllByStatusIdIn(List<HistoryNotificationTaskStatus> statuses);
    void deleteAllByStatusIdInAndCreatedAtBefore(List<HistoryNotificationTaskStatus> needStartStatuses, Timestamp createdAt);
    long countAllByStatusIdInAndCreatedAtBefore(List<HistoryNotificationTaskStatus> needStartStatuses, Timestamp createdAt);
}
