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
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl JOIN twin t2 ON tl.dst_twin_id = t2.id WHERE t.id = tl.src_twin_id AND t2.hierarchy_tree <@ (SELECT hierarchy_tree FROM twin WHERE id = :twinId) AND tl.link_id = :linkId AND t.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByTwinClassIdAndLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl WHERE t.id = tl.dst_twin_id AND tl.src_twin_id = :twinId AND tl.link_id = :linkId AND (:twinClassId IS NULL OR t.twin_class_id = :twinClassId)", nativeQuery = true)
    int updateTwinStatusByDstTwinIdAndLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId WHERE t.id = (SELECT head_twin_id FROM twin WHERE id = (SELECT head_twin_id FROM twin WHERE id = :twinId))", nativeQuery = true)
    void updateTwinStatusByGrandParentId(@Param("twinId") UUID twinId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl JOIN twin parent ON tl.src_twin_id = parent.id WHERE t.id = tl.dst_twin_id AND parent.id = (SELECT head_twin_id FROM twin WHERE id = :twinId) AND tl.link_id = :linkId", nativeQuery = true)
    void updateTwinStatusByHeadTwinThenLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl_wt JOIN twin tasks ON tl_wt.src_twin_id = tasks.id JOIN twin_link tl_sp ON tl_sp.dst_twin_id = tasks.id WHERE t.id = tl_sp.src_twin_id AND tl_wt.dst_twin_id = :twinId AND tl_wt.link_id = :firstLinkId AND tl_sp.link_id = :secondLinkId AND t.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByTwoLinks(@Param("twinId") UUID twinId, @Param("firstLinkId") UUID firstLinkId, @Param("secondLinkId") UUID secondLinkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl_sp JOIN twin tasks ON tl_sp.dst_twin_id = tasks.id JOIN twin_link tl_wt ON tl_wt.src_twin_id = tasks.id WHERE t.id = tl_wt.dst_twin_id AND tl_sp.src_twin_id = :twinId AND tl_sp.link_id = :firstLinkId AND tl_wt.link_id = :secondLinkId AND (:twinClassId IS NULL OR t.twin_class_id = :twinClassId)", nativeQuery = true)
    int updateTwinStatusByTwoLinksForward(@Param("twinId") UUID twinId, @Param("firstLinkId") UUID firstLinkId, @Param("secondLinkId") UUID secondLinkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl JOIN twin tasks ON tl.src_twin_id = tasks.id WHERE t.head_twin_id = tasks.id AND tl.dst_twin_id = :twinId AND tl.link_id = :linkId AND t.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByLinkAndHeadTwinChildren(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl WHERE t.id = tl.dst_twin_id AND tl.src_twin_id = (SELECT head_twin_id FROM twin WHERE id = :twinId) AND tl.link_id = :linkId AND (:twinClassId IS NULL OR t.twin_class_id = :twinClassId)", nativeQuery = true)
    int updateTwinStatusByHeadThenLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin t SET twin_status_id = :statusId FROM twin_link tl WHERE t.id = tl.dst_twin_id AND tl.src_twin_id = :twinId AND tl.link_id = :linkId AND (:twinClassId IS NULL OR t.twin_class_id = :twinClassId)", nativeQuery = true)
    int updateTwinStatusBySrcTwinIdAndLinkId(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE twin project SET twin_status_id = :statusId FROM twin_link tl JOIN twin task ON tl.dst_twin_id = task.id WHERE project.id = task.head_twin_id AND tl.src_twin_id = :twinId AND tl.link_id = :linkId AND project.twin_class_id = :twinClassId", nativeQuery = true)
    int updateTwinStatusByLinkAndHead(@Param("twinId") UUID twinId, @Param("linkId") UUID linkId, @Param("twinClassId") UUID twinClassId, @Param("statusId") UUID statusId);
}
