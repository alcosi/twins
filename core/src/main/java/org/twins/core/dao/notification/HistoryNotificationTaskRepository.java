package org.twins.core.dao.notification;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
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
     * Queue-claim select: FOR UPDATE SKIP LOCKED (hint -2), ordered by createdAt for FIFO. Must run in
     * the same transaction as the follow-up IN_PROGRESS bulk update — see
     * {@code HistoryNotificationTaskService.collectAndMarkInProgress}: the row locks make the claim
     * atomic across overlapping scheduler ticks (and app nodes), so no two transactions ever update
     * overlapping task rows → no deadlocks, no double dispatch.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select t from HistoryNotificationTaskEntity t where t.statusId in :statusIds order by t.createdAt")
    List<HistoryNotificationTaskEntity> findClaimableByStatusIdIn(@Param("statusIds") Collection<HistoryNotificationTaskStatus> statusIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select t from HistoryNotificationTaskEntity t where t.statusId in :statusIds order by t.createdAt")
    List<HistoryNotificationTaskEntity> findClaimableByStatusIdIn(@Param("statusIds") Collection<HistoryNotificationTaskStatus> statusIds, Pageable pageable);

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
