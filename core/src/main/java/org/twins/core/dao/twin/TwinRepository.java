package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.EntryCount;
import org.twins.core.dao.user.UserEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinRepository extends JpaRepository<TwinEntity, UUID>, JpaSpecificationExecutor<TwinEntity>, PagingAndSortingRepository<TwinEntity, UUID> {
    List<TwinEntity> findByTwinClassDomainId(UUID domainId);

    List<TwinEntity> findByOwnerBusinessAccountId(UUID businessAccount);

    List<TwinEntity> findByTwinClassId(UUID twinClassId);

    List<TwinEntity> findByIdIn(Collection<UUID> twinIds);

    boolean existsByTwinClassId(UUID twinClassId);

    @Query(value = "select t.assignerUser from TwinEntity t where t.id = :twinId")
    UserEntity getAssignee(@Param("twinId") UUID twinId);

    @Modifying
    @Query("delete from TwinEntity te where te.ownerBusinessAccountId = :businessAccountId and te.twinClass.domainId = :domainId")
    int deleteAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

    @Query(value = "select distinct t.headTwinId from TwinEntity t where t.twinClassId = :twinClassId and t.headTwinId is not null")
    Set<UUID> findDistinctHeadTwinIdByTwinClassId(UUID twinClassId);

    @Transactional
    @Modifying
    @Query(value = "update TwinEntity set headTwinId = :newVal where headTwinId = :oldVal and twinClassId = :twinClassId")
    void replaceHeadTwinForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("oldVal") UUID oldVal, @Param("newVal") UUID newVal);

    @Query(value = "select t.id from TwinEntity t where t.twinClassId = :twinClassId and t.id in :ids")
    Set<UUID> findIdByTwinClassIdAndIdIn(@Param("twinClassId") UUID twinClassId, @Param("ids") Collection<UUID> ids);

    <T> List<T> findByHeadTwinIdInAndTwinClassIdIn(Collection<UUID> headTwinIds, Collection<UUID> twinClassIds, Class<T> clazz);

    @Query(value = "select t from TwinEntity t where t.headTwinId in :headTwinIds and t.twinClass.segment = true ")
    List<TwinEntity> findSegments(Collection<UUID> headTwinIds);

    @Query(value = "select h from TwinEntity t, TwinEntity h where t.id = :twinId and t.headTwinId = h.id")
    TwinEntity findHeadTwin(@Param("twinId") UUID twinId);

    @Query(value = "select count(p) from permission_schema_detect_mismatches() p", nativeQuery = true)
    long countPermissionSchemaMismatches();

    @Query(value = "SELECT t.owner_business_account_id AS id, COUNT(t) AS count FROM twin t JOIN twin_class tc on t.twin_class_id = tc.id WHERE t.owner_business_account_id IN :businessAccountIds and tc.domain_id = :domainId GROUP BY t.owner_business_account_id",
            nativeQuery = true)
    List<EntryCount> countTwinsInBusinessAccounts(@Param("businessAccountIds") Collection<UUID> businessAccountIds, @Param("domainId") UUID domainId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl JOIN twin t2 ON tl.dst_twin_id = t2.id WHERE t.id = tl.src_twin_id AND t2.hierarchy_tree <@ CAST(:hierarchyTree AS ltree) AND tl.link_id = :linkId AND t.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByTwinClassIdAndLinkId(@Param("twinId") UUID twinId, @Param("hierarchyTree") String hierarchyTree, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t SET t.twinStatusId = :statusId " +
           "WHERE t.id IN (SELECT tl2.srcTwinId FROM TwinLinkEntity tl2 " +
           "WHERE tl2.linkId = :secondLinkId " +
           "AND tl2.dstTwinId IN (SELECT tl1.srcTwinId FROM TwinLinkEntity tl1 " +
           "WHERE tl1.dstTwinId = :twinId AND tl1.linkId = :firstLinkId)) " +
           "AND t.twinClassId = :twinClassId")
    int updateTwinStatusByTwoLinks(@Param("twinId") UUID twinId, @Param("firstLinkId") UUID firstLinkId, @Param("secondLinkId") UUID secondLinkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t SET t.twinStatusId = :statusId " +
           "WHERE t.id IN (SELECT tl2.dstTwinId FROM TwinLinkEntity tl2 " +
           "WHERE tl2.linkId = :secondLinkId " +
           "AND tl2.srcTwinId IN (SELECT tl1.dstTwinId FROM TwinLinkEntity tl1 " +
           "WHERE tl1.srcTwinId = :twinId AND tl1.linkId = :firstLinkId)) " +
           "AND (:twinClassId IS NULL OR t.twinClassId = :twinClassId)")
    int updateTwinStatusByTwoLinksForward(@Param("twinId") UUID twinId, @Param("firstLinkId") UUID firstLinkId, @Param("secondLinkId") UUID secondLinkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t SET t.twinStatusId = :statusId " +
           "WHERE t.headTwinId IN (SELECT tl.srcTwinId FROM TwinLinkEntity tl " +
           "WHERE tl.dstTwinId = :twinId AND tl.linkId = :linkId) " +
           "AND t.twinClassId = :twinClassId")
    int updateTwinStatusByLinkAndHeadTwinChildren(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t SET t.twinStatusId = :statusId " +
           "WHERE t.id IN (SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
           "WHERE tl.srcTwinId = :headTwinId " +
           "AND tl.linkId = :linkId) " +
           "AND (:twinClassId IS NULL OR t.twinClassId = :twinClassId)")
    int updateTwinStatusByHeadThenLinkId(@Param("twinId") UUID twinId, @Param("headTwinId") UUID headTwinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t SET t.twinStatusId = :statusId " +
           "WHERE t.id IN (SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
           "WHERE tl.srcTwinId = :twinId AND tl.linkId = :linkId) " +
           "AND (:twinClassId IS NULL OR t.twinClassId = :twinClassId)")
    int updateTwinStatusBySrcTwinIdAndLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity project SET project.twinStatusId = :statusId " +
           "WHERE project.id IN (SELECT task.headTwinId FROM TwinEntity task " +
           "WHERE task.id IN (SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
           "WHERE tl.srcTwinId = :twinId AND tl.linkId = :linkId)) " +
           "AND project.twinClassId = :twinClassId")
    int updateTwinStatusByLinkAndHead(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId WHERE CAST(:hierarchyTree AS ltree) <@ t.hierarchy_tree AND t.id != :twinId AND (:maxDepth < 0 OR nlevel(t.hierarchy_tree) - nlevel(CAST(:hierarchyTree AS ltree)) <= :maxDepth) AND t.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByHeadAncestors(@Param("twinId") UUID twinId, @Param("hierarchyTree") String hierarchyTree, @Param("maxDepth") int maxDepth, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId WHERE t.hierarchy_tree <@ CAST(:hierarchyTree AS ltree) AND t.id != :twinId AND (:maxDepth < 0 OR nlevel(CAST(:hierarchyTree AS ltree)) - nlevel(t.hierarchy_tree) <= :maxDepth) AND t.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByHeadDescendants(@Param("twinId") UUID twinId, @Param("hierarchyTree") String hierarchyTree, @Param("maxDepth") int maxDepth, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t SET t.twinStatusId = :statusId " +
           "WHERE t.id IN (SELECT tl.srcTwinId FROM TwinLinkEntity tl " +
           "WHERE tl.dstTwinId = :twinId AND tl.linkId = :linkId)")
    int updateTwinStatusByDstTwinIdAndLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity head SET head.twinStatusId = :headStatusId " +
           "WHERE head.id = :headTwinId " +
           "AND (:headTwinClassId IS NULL OR head.twinClassId = :headTwinClassId) " +
           "AND NOT EXISTS (" +
           "SELECT child.id FROM TwinEntity child " +
           "WHERE child.headTwinId = :headTwinId " +
           "AND child.id <> :currentTwinId " +
           "AND child.twinStatusId IN :childrenStatuses)")
    int updateHeadStatusIfNoChildrenInStatuses(
            @Param("headTwinId") UUID headTwinId,
            @Param("currentTwinId") UUID currentTwinId,
            @Param("childrenStatuses") Collection<UUID> childrenStatuses,
            @Param("headTwinClassId") UUID headTwinClassId,
            @Param("headStatusId") UUID headStatusId);

    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity dst SET dst.twinStatusId = :dstStatusId " +
           "WHERE dst.id IN (" +
           "  SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
           "  WHERE tl.srcTwinId = :currentTwinId " +
           "  AND tl.linkId = :linkId" +
           ") " +
           "AND NOT EXISTS (" +
           "  SELECT tl2.id FROM TwinLinkEntity tl2 " +
           "  JOIN TwinEntity src ON src.id = tl2.srcTwinId " +
           "  WHERE tl2.dstTwinId = dst.id " +
           "  AND tl2.linkId = :linkId " +
           "  AND tl2.srcTwinId <> :currentTwinId " +
           "  AND src.twinStatusId IN :srcTwinsStatuses)")
    int updateDstTwinStatusByLinkIfNoLinkedTwinsInStatuses(
            @Param("currentTwinId") UUID currentTwinId,
            @Param("linkId") UUID linkId,
            @Param("srcTwinsStatuses") Collection<UUID> srcTwinsStatuses,
            @Param("dstStatusId") UUID dstStatusId);

    @Query("select case when count(child) > 0 then true else false end " +
           "from TwinEntity child " +
           "where child.headTwinId in (" +
           "  select tl.srcTwinId from TwinLinkEntity tl " +
           "  where tl.dstTwinId = :twinId and tl.linkId = :linkId" +
           ") " +
           "and child.twinStatusId in :statusIds")
    boolean existsChildrenByBackwardLinkAndStatuses(
            @Param("twinId") UUID twinId,
            @Param("linkId") UUID linkId,
            @Param("statusIds") Collection<UUID> statusIds);

    @Query("select distinct tl.dstTwinId, t from TwinEntity t, TwinLinkEntity tl "
            + "where tl.dstTwinId in :linkDstTwinIds "
            + "and tl.linkId = :linkId "
            + "and t.ownerBusinessAccountId = :ownerBusinessAccountId "
            + "and t.headTwinId in ("
            + "select tl2.srcTwinId from TwinLinkEntity tl2 "
            + "where tl2.dstTwinId = tl.dstTwinId and tl2.linkId = :linkId)")
    List<Object[]> findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwins(
            @Param("linkDstTwinIds") Collection<UUID> linkDstTwinIds,
            @Param("linkId") UUID linkId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId);

    @Query("select distinct tl.dstTwinId, t from TwinEntity t, TwinLinkEntity tl "
            + "where tl.dstTwinId in :linkDstTwinIds "
            + "and tl.linkId = :linkId "
            + "and t.ownerBusinessAccountId = :ownerBusinessAccountId "
            + "and t.headTwinId in ("
            + "select tl2.srcTwinId from TwinLinkEntity tl2 "
            + "where tl2.dstTwinId = tl.dstTwinId and tl2.linkId = :linkId) "
            + "and t.twinStatusId in :statusIds")
    List<Object[]> findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesIncluded(
            @Param("linkDstTwinIds") Collection<UUID> linkDstTwinIds,
            @Param("linkId") UUID linkId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("statusIds") Collection<UUID> statusIds);

    @Query("select distinct tl.dstTwinId, t from TwinEntity t, TwinLinkEntity tl "
            + "where tl.dstTwinId in :linkDstTwinIds "
            + "and tl.linkId = :linkId "
            + "and t.ownerBusinessAccountId = :ownerBusinessAccountId "
            + "and t.headTwinId in ("
            + "select tl2.srcTwinId from TwinLinkEntity tl2 "
            + "where tl2.dstTwinId = tl.dstTwinId and tl2.linkId = :linkId) "
            + "and t.twinStatusId not in :statusIds")
    List<Object[]> findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesExcluded(
            @Param("linkDstTwinIds") Collection<UUID> linkDstTwinIds,
            @Param("linkId") UUID linkId,
            @Param("ownerBusinessAccountId") UUID ownerBusinessAccountId,
            @Param("statusIds") Collection<UUID> statusIds);


    @Query(value = "SELECT twins_quota_get(:twinClassSchemaSpaceId, :domainId, :businessAccountId, :twinClassId)", nativeQuery = true)
    Integer getTwinsQuota(@Param("twinClassSchemaSpaceId") UUID twinClassSchemaSpaceId, @Param("domainId") UUID domainId, @Param("businessAccountId") UUID businessAccountId, @Param("twinClassId") UUID twinClassId);

    @Query(value = """
            SELECT COUNT(*)
            FROM twin t
            WHERE t.owner_business_account_id IS NOT DISTINCT FROM :businessAccountId
              AND t.twin_class_id = :twinClassId
              AND t.twin_class_schema_space_id IS NOT DISTINCT FROM :twinClassSchemaSpaceId
        """, nativeQuery = true)
    long countTwinsByQuotaKey(@Param("twinClassSchemaSpaceId") UUID twinClassSchemaSpaceId, @Param("businessAccountId") UUID businessAccountId, @Param("twinClassId") UUID twinClassId);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and not child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusNotIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(child.headTwinId, cast(count(child) as bigdecimal))
        from TwinEntity child
        where child.headTwinId in :headTwinIdList and child.twinStatusId in :childrenTwinStatusIdList
        group by child.headTwinId
        """)
    List<TwinFieldCalcProjection> countChildrenTwinsWithStatusIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(child.headTwinId, cast(count(child) as bigdecimal))
        from TwinEntity child
        where child.headTwinId in :headTwinIdList and not child.twinStatusId in :childrenTwinStatusIdList
        group by child.headTwinId
        """)
    List<TwinFieldCalcProjection> countChildrenTwinsWithStatusNotIn(
            @Param("headTwinIdList") Collection<UUID> headTwinIdList,
            @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(tl.dstTwinId, cast(count(tl) as bigdecimal))
        from TwinLinkEntity tl
        join TwinEntity t on t.id = tl.srcTwinId
        where tl.dstTwinId in :dstTwinIdList and tl.linkId in :linkIds and t.twinStatusId in :linkedTwinStatusIdList
        group by tl.dstTwinId
        """)
    List<TwinFieldCalcProjection> countLinkedTwinsByBackwardLinkWithStatusIn(
            @Param("dstTwinIdList") Collection<UUID> dstTwinIdList,
            @Param("linkIds") Collection<UUID> linkIds,
            @Param("linkedTwinStatusIdList") Collection<UUID> linkedTwinStatusIdList);

    @Query(value = """
        select new org.twins.core.dao.twin.TwinFieldCalcProjection(tl.dstTwinId, cast(count(tl) as bigdecimal))
        from TwinLinkEntity tl
        join TwinEntity t on t.id = tl.srcTwinId
        where tl.dstTwinId in :dstTwinIdList and tl.linkId in :linkIds and t.twinStatusId not in :linkedTwinStatusIdList
        group by tl.dstTwinId
        """)
    List<TwinFieldCalcProjection> countLinkedTwinsByBackwardLinkWithStatusNotIn(
            @Param("dstTwinIdList") Collection<UUID> dstTwinIdList,
            @Param("linkIds") Collection<UUID> linkIds,
            @Param("linkedTwinStatusIdList") Collection<UUID> linkedTwinStatusIdList);

    // Grandparent status change when all grandchildren have the same status - 4 cases
    // Two-hop navigation: t1 (current) -> t2 (intermediate) -> t3 (grandparent)

    // Case 1: head-head
    // t1 -> t2 by head (t1.headTwinId = t2.id)
    // t2 -> t3 by head (t2.headTwinId = t3.id)
    // Check all children of t3 by head (only children with same twinClassId as t2)
    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t3 SET t3.twinStatusId = :parentTargetStatusId " +
            "WHERE t3.id IN (" +
            "  SELECT t2.headTwinId FROM TwinEntity t2 " +
            "  WHERE t2.id = (" +
            "    SELECT t1.headTwinId FROM TwinEntity t1 " +
            "    WHERE t1.id = :currentTwinId" +
            "  ) " +
            "  AND t2.headTwinId IS NOT NULL" +
            ") " +
            "AND NOT EXISTS (" +
            "  SELECT child.id FROM TwinEntity child " +
            "  WHERE child.headTwinId = t3.id " +
            "  AND child.twinClassId = (" +
            "    SELECT t2.twinClassId FROM TwinEntity t2 " +
            "    WHERE t2.id = (" +
            "      SELECT t1.headTwinId FROM TwinEntity t1 " +
            "      WHERE t1.id = :currentTwinId" +
            "    )" +
            "  ) " +
            "  AND child.twinStatusId <> :checkChildrenStatusId)")
    int updateGrandparentStatusIfAllGrandchildrenInStatusHeadHead(
            @Param("currentTwinId") UUID currentTwinId,
            @Param("parentTargetStatusId") UUID parentTargetStatusId,
            @Param("checkChildrenStatusId") UUID checkChildrenStatusId);

    // Case 2: head-link
    // t1 -> t2 by head (t1.headTwinId = t2.id)
    // t2 -> t3 by link (t2 link -> t3)
    // Check all children of t3 by link (only children with same twinClassId as t2)
    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t3 SET t3.twinStatusId = :parentTargetStatusId " +
            "WHERE t3.id IN (" +
            "  SELECT tl2.dstTwinId FROM TwinLinkEntity tl2 " +
            "  WHERE tl2.srcTwinId = (" +
            "    SELECT t1.headTwinId FROM TwinEntity t1 " +
            "    WHERE t1.id = :currentTwinId" +
            "  ) " +
            "  AND tl2.linkId = :linkId" +
            ") " +
            "AND NOT EXISTS (" +
            "  SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
            "  JOIN TwinEntity child ON child.id = tl.dstTwinId " +
            "  WHERE tl.srcTwinId = t3.id " +
            "  AND tl.linkId = :linkId " +
            "  AND child.twinClassId = (" +
            "    SELECT t2.twinClassId FROM TwinEntity t2 " +
            "    WHERE t2.id = (" +
            "      SELECT t1.headTwinId FROM TwinEntity t1 " +
            "      WHERE t1.id = :currentTwinId" +
            "    )" +
            "  ) " +
            "  AND child.twinStatusId <> :checkChildrenStatusId)")
    int updateGrandparentStatusIfAllGrandchildrenInStatusHeadLink(
            @Param("currentTwinId") UUID currentTwinId,
            @Param("linkId") UUID linkId,
            @Param("parentTargetStatusId") UUID parentTargetStatusId,
            @Param("checkChildrenStatusId") UUID checkChildrenStatusId);

    // Case 3: link-link
    // t1 -> t2 by link (t1 link -> t2)
    // t2 -> t3 by link (t2 link -> t3)
    // Check all children of t3 by link (only children with same twinClassId as t2)
    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t3 SET t3.twinStatusId = :parentTargetStatusId " +
            "WHERE t3.id IN (" +
            "  SELECT tl2.dstTwinId FROM TwinLinkEntity tl2 " +
            "  WHERE tl2.srcTwinId IN (" +
            "    SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
            "    WHERE tl.srcTwinId = :currentTwinId " +
            "    AND tl.linkId = :firstLinkId" +
            "  ) " +
            "  AND tl2.linkId = :secondLinkId" +
            ") " +
            "AND NOT EXISTS (" +
            "  SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
            "  JOIN TwinEntity child ON child.id = tl.dstTwinId " +
            "  WHERE tl.srcTwinId = t3.id " +
            "  AND tl.linkId = :secondLinkId " +
            "  AND child.twinClassId IN (" +
            "    SELECT tl2Src.twinClassId FROM TwinEntity tl2Src " +
            "    WHERE tl2Src.id IN (" +
            "      SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
            "      WHERE tl.srcTwinId = :currentTwinId " +
            "      AND tl.linkId = :firstLinkId" +
            "    )" +
            "  ) " +
            "  AND child.twinStatusId <> :checkChildrenStatusId)")
    int updateGrandparentStatusIfAllGrandchildrenInStatusLinkLink(
            @Param("currentTwinId") UUID currentTwinId,
            @Param("firstLinkId") UUID firstLinkId,
            @Param("secondLinkId") UUID secondLinkId,
            @Param("parentTargetStatusId") UUID parentTargetStatusId,
            @Param("checkChildrenStatusId") UUID checkChildrenStatusId);

    // Case 4: link-head
    // t1 -> t2 by link (t1 link -> t2)
    // t2 -> t3 by head (t2.headTwinId = т3.id)
    // Check all children of т3 by head (only children with same twinClassId as t2)
    @Modifying
    @Transactional
    @Query("UPDATE TwinEntity t3 SET t3.twinStatusId = :parentTargetStatusId " +
            "WHERE t3.id IN (" +
            "  SELECT t2.headTwinId FROM TwinEntity t2 " +
            "  WHERE t2.id IN (" +
            "    SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
            "    WHERE tl.srcTwinId = :currentTwinId " +
            "    AND tl.linkId = :linkId" +
            "  ) " +
            "  AND t2.headTwinId IS NOT NULL" +
            ") " +
            "AND NOT EXISTS (" +
            "  SELECT child.id FROM TwinEntity child " +
            "  WHERE child.headTwinId = t3.id " +
            "  AND child.twinClassId IN (" +
            "    SELECT t2.twinClassId FROM TwinEntity t2 " +
            "    WHERE t2.id IN (" +
            "      SELECT tl.dstTwinId FROM TwinLinkEntity tl " +
            "      WHERE tl.srcTwinId = :currentTwinId " +
            "      AND tl.linkId = :linkId" +
            "    )" +
            "  ) " +
            "  AND child.twinStatusId <> :checkChildrenStatusId)")
    int updateGrandparentStatusIfAllGrandchildrenInStatusLinkHead(
            @Param("currentTwinId") UUID currentTwinId,
            @Param("linkId") UUID linkId,
            @Param("parentTargetStatusId") UUID parentTargetStatusId,
            @Param("checkChildrenStatusId") UUID checkChildrenStatusId);
}
