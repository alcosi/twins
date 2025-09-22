package org.twins.core.dao.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends CrudRepository<HistoryEntity, UUID>, JpaSpecificationExecutor<HistoryEntity> {
    Page<HistoryEntity> findByTwinId(UUID twinId, Pageable pageable);

    //todo check performance
//    @Query(value = "select he from HistoryEntity he left join TwinEntity te on he.twinId = te.id where he.twinId = :twinId or te.headTwinId = :twinId")
    @Query(value = "select he from HistoryEntity he where he.twinId = :twinId " +
            "or he.twinId in (select child.id from TwinEntity child where child.headTwinId = :twinId)")
    Page<HistoryEntity> findByTwinIdIncludeFirstLevelChildren(@Param("twinId") UUID twinId, Pageable pageable);

    /**
     * Returns for each twin that has a history entry created before {@code before} timestamp:
     * a list of distinct user ids that have subscription enabled and are either the creator
     * or the assigner of the twin.
     *
     * @param before look-back period
     */
    @Query(value = "WITH recent AS ( " +
            "SELECT id, twin_id " +
            "FROM history " +
            "WHERE created_at >= :before ) " +
            "SELECT r.twin_id AS twinId, t.domain_id as domainId, " +
            "array_agg(DISTINCT du.user_id) AS userIds, " +
            "array_agg(r.id) AS historyIds " +
            "FROM recent r " +
            "JOIN twin t ON t.id = r.twin_id " +
            "JOIN domain_user du ON du.subscription_enabled = TRUE " +
            "AND du.user_id = ANY (ARRAY[t.created_by_user_id, t.assigner_user_id, t.owner_user_id]) " +
            "GROUP BY r.twin_id, t.domain_id",
            nativeQuery = true)
    List<TwinUsersProjection> findRecentHistoryItems(@Param("before") Timestamp before);

    interface TwinUsersProjection {
        UUID getTwinId();

        UUID getDomainId();

        java.util.UUID[] getUserIds();

        java.util.UUID[] getHistoryIds();
    }

    /**
     * Picks a batch of NEW history rows, marks them IN_PROGRESS and returns per-twin aggregated ids.
     */
    @Modifying
    @Query(value = """
            WITH moved AS (
                UPDATE history
                   SET dispatch_status = 'IN_PROGRESS'
                 WHERE dispatch_status = 'NEW'
                   AND created_at >= :before
                   AND history_type_id = ANY(:historyTypes)
                 ORDER BY created_at
                 LIMIT :limit
                 RETURNING id, twin_id
            )
            SELECT  m.twin_id            AS twinId,
                    t.domain_id          AS domainId,
                    array_agg(m.id)      AS historyIds
            FROM moved m
            JOIN twin t ON t.id = m.twin_id
            GROUP BY m.twin_id, t.domain_id
            """, nativeQuery = true)
    List<PickedBatch> pickBatch(@Param("before") Timestamp before, @Param("limit") int limit, List<HistoryType> historyTypes);

    interface PickedBatch {
        UUID getTwinId();
        UUID getDomainId();
        java.util.UUID[] getHistoryIds();
    }

    /**
     * Returns user lists for the provided twin ids. historyIds are returned as empty array and
     * will be filled later in service layer.
     */
    @Query(value = """
            SELECT t.id AS twinId,
                   t.domain_id AS domainId,
                   array_agg(DISTINCT du.user_id) AS userIds,
                   array[]::uuid[]                AS historyIds
            FROM twin t
            JOIN domain_user du ON du.subscription_enabled = TRUE
                 AND du.user_id = ANY (ARRAY[t.created_by_user_id, t.assigner_user_id, t.owner_user_id])
            WHERE t.id IN (:twinIds)
            GROUP BY t.id, t.domain_id
            """, nativeQuery = true)
    List<TwinUsersProjection> findUsersForTwins(@Param("twinIds") Collection<UUID> twinIds);

    @Modifying
    @Query("UPDATE HistoryEntity h SET h.dispatch_status = :dispatchStatus WHERE h.id in (:ids) ")
    int updateAllNotified(@Param("ids") Collection<UUID> ids, @Param("dispatchStatus") HistoryDispatchStatus dispatchStatus);


}
