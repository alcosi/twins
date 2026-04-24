package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinLinkRepository extends CrudRepository<TwinLinkEntity, UUID>, JpaSpecificationExecutor<TwinLinkEntity> {
    List<TwinLinkEntity> findBySrcTwinIdOrDstTwinId(UUID srcTwinId, UUID dstTwinId);
    List<TwinLinkEntity> findBySrcTwinIdInOrDstTwinIdIn(Set<UUID> srcTwinIdList, Set<UUID> dstTwinIdList);
    List<TwinLinkEntity> findBySrcTwinIdIn(Set<UUID> srcTwinIdList);

    @Modifying
    @Query(value = "from TwinLinkEntity twinLink " +
            "where twinLink.id in (:linkIdList) and (twinLink.srcTwinId = :twinId or twinLink.dstTwinId = :twinId)")
    void deleteNotMandatory(@Param("twinId") UUID twinId, @Param("linkIdList") List<UUID> twinLinksDeleteUUIDList);

    <T> List<T> findBySrcTwinId(UUID srcTwinId, Class<T> type);
    <T> List<T> findBySrcTwinIdAndLinkIdIn(UUID srcTwinId, Collection<UUID> linkIdList,  Class<T> type);

    <T> List<T> findByDstTwinId(UUID dstTwinId, Class<T> type);
    <T> List<T> findBySrcTwinIdAndLinkId(UUID srcTwinId, UUID linkId, Class<T> type);
    <T> List<T> findByDstTwinIdAndLinkId(UUID dstTwinId, UUID linkId, Class<T> type);

    boolean existsBySrcTwinIdAndLinkId(UUID srcTwinId, UUID linkId);

    boolean existsByDstTwinIdAndLinkId(UUID srcTwinId, UUID linkId);

    <T> T findBySrcTwinIdAndDstTwinIdAndLinkId(UUID srcTwinId, UUID dstTwinId, UUID linkId, Class<T> type);

    @Query("""
            select (count(tl) > 0)
            from TwinLinkEntity tl
            join TwinEntity src on src.id = tl.srcTwinId
            join TwinFieldDecimalEntity tfd on tfd.twinId = src.id
            where tl.dstTwinId = :dstTwinId
              and tl.linkId = :linkId
              and src.twinStatusId in :srcStatusIds
              and tfd.twinClassFieldId = :twinClassFieldId
              and tfd.value > 0
            """)
    boolean existsDstTwinLinkedFromSrcWithStatusAndPositiveDecimalField(
            @Param("dstTwinId") UUID dstTwinId,
            @Param("linkId") UUID linkId,
            @Param("srcStatusIds") Collection<UUID> srcStatusIds,
            @Param("twinClassFieldId") UUID twinClassFieldId
    );

    @Query("""
            select distinct tl.dstTwinId
            from TwinLinkEntity tl
            join TwinEntity src on src.id = tl.srcTwinId
            join TwinFieldDecimalEntity tfd on tfd.twinId = src.id
            where tl.dstTwinId in :dstTwinIds
              and tl.linkId = :linkId
              and src.twinStatusId in :srcStatusIds
              and tfd.twinClassFieldId = :twinClassFieldId
              and tfd.value > 0
            """)
    Set<UUID> findDstTwinIdsLinkedFromSrcWithStatusAndPositiveDecimalField(
            @Param("dstTwinIds") Collection<UUID> dstTwinIds,
            @Param("linkId") UUID linkId,
            @Param("srcStatusIds") Collection<UUID> srcStatusIds,
            @Param("twinClassFieldId") UUID twinClassFieldId
    );

    @Query(value = "select distinct srcTwinId from TwinLinkEntity where linkId = :linkId")
    Set<UUID> findSrcTwinIdsByLinkId(@Param("linkId") UUID linkId);

    @Query(value = "select distinct dstTwinId from TwinLinkEntity where linkId = :linkId")
    Set<UUID> findDstTwinIdsByLinkId(@Param("linkId") UUID linkId);


    //todo think about batch query
    @Transactional
    @Modifying
    @Query(value = "update TwinLinkEntity set srcTwinId = :newVal where srcTwinId = :oldVal and linkId = :linkId")
    void replaceSrcTwinIdForTwinLinkByLinkId(@Param("linkId") UUID linkId, @Param("oldVal") UUID oldVal, @Param("newVal") UUID newVal);

    @Transactional
    @Modifying
    @Query(value = "update TwinLinkEntity set dstTwinId = :newVal where dstTwinId = :oldVal and linkId = :linkId")
    void replaceDstTwinIdForTwinLinkByLinkId(@Param("linkId") UUID linkId, @Param("oldVal") UUID oldVal, @Param("newVal") UUID newVal);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            JOIN twin src ON tl.src_twin_id = src.id
            JOIN twin dst ON tl.dst_twin_id = dst.id
            WHERE EXISTS (
                SELECT 1 FROM twin h
                WHERE h.id IN :hierarchyTwinIds
                  AND src.hierarchy_tree <@ h.hierarchy_tree
                  AND dst.hierarchy_tree <@ h.hierarchy_tree
            )
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllWithinHierarchies(@Param("hierarchyTwinIds") Collection<UUID> hierarchyTwinIds);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            JOIN twin src ON tl.src_twin_id = src.id
            JOIN twin dst ON tl.dst_twin_id = dst.id
            WHERE tl.link_id IN :linkIds
              AND EXISTS (
                SELECT 1 FROM twin h
                WHERE h.id IN :hierarchyTwinIds
                  AND src.hierarchy_tree <@ h.hierarchy_tree
                  AND dst.hierarchy_tree <@ h.hierarchy_tree
            )
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllWithinHierarchiesAndLinkIdIn(@Param("hierarchyTwinIds") Collection<UUID> hierarchyTwinIds, @Param("linkIds") Collection<UUID> linkIds);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            JOIN twin src ON tl.src_twin_id = src.id
            JOIN twin dst ON tl.dst_twin_id = dst.id
            WHERE src.twin_status_id IN :twinStatusIds
              AND dst.twin_status_id IN :twinStatusIds
              AND EXISTS (
                  SELECT 1 FROM twin h
                  WHERE h.id IN :hierarchyTwinIds
                    AND src.hierarchy_tree <@ h.hierarchy_tree
                    AND dst.hierarchy_tree <@ h.hierarchy_tree
              )
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllWithinHierarchiesAndTwinsInStatusIds(@Param("hierarchyTwinIds") Collection<UUID> hierarchyTwinIds, @Param("twinStatusIds") Collection<UUID> twinStatusIds);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            JOIN twin src ON tl.src_twin_id = src.id
            JOIN twin dst ON tl.dst_twin_id = dst.id
            WHERE src.twin_status_id IN :twinStatusIds
              AND dst.twin_status_id IN :twinStatusIds
              AND tl.link_id in :linkIds
              AND EXISTS (
                  SELECT 1 FROM twin h
                  WHERE h.id IN :hierarchyTwinIds
                    AND src.hierarchy_tree <@ h.hierarchy_tree
                    AND dst.hierarchy_tree <@ h.hierarchy_tree
              )
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllWithinHierarchiesAndLinkIdInAndTwinsInStatusIds(@Param("hierarchyTwinIds") Collection<UUID> hierarchyTwinIds, @Param("linkIds") Collection<UUID> linkIds, @Param("twinStatusIds") Collection<UUID> twinStatusIds);

    /**
     * Forward links whose both endpoints are in {@code twinIds} (membership in the set, no ltree scope).
     */
    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            WHERE tl.src_twin_id IN :twinIds
              AND tl.dst_twin_id IN :twinIds
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllBetweenTwinsIn(@Param("twinIds") Collection<UUID> twinIds);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            JOIN twin src ON tl.src_twin_id = src.id
            JOIN twin dst ON tl.dst_twin_id = dst.id
            WHERE tl.src_twin_id IN :twinIds
              AND tl.dst_twin_id IN :twinIds
              AND src.twin_status_id IN :twinStatusIds
              AND dst.twin_status_id IN :twinStatusIds
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllBetweenTwinsInAndTwinsInStatusIds(@Param("twinIds") Collection<UUID> twinIds, @Param("twinStatusIds") Collection<UUID> twinStatusIds);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            WHERE tl.src_twin_id IN :twinIds
              AND tl.dst_twin_id IN :twinIds
              AND tl.link_id IN :linkIds
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllBetweenTwinsInAndLinkIdIn(@Param("twinIds") Collection<UUID> twinIds, @Param("linkIds") Collection<UUID> linkIds);

    @Query(value = """
            SELECT tl.*
            FROM twin_link tl
            JOIN twin src ON tl.src_twin_id = src.id
            JOIN twin dst ON tl.dst_twin_id = dst.id
            WHERE tl.src_twin_id IN :twinIds
              AND tl.dst_twin_id IN :twinIds
              AND tl.link_id IN :linkIds
              AND src.twin_status_id IN :twinStatusIds
              AND dst.twin_status_id IN :twinStatusIds
            """, nativeQuery = true)
    Set<TwinLinkEntity> findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIds(@Param("twinIds") Collection<UUID> twinIds, @Param("linkIds") Collection<UUID> linkIds, @Param("twinStatusIds") Collection<UUID> twinStatusIds);
}
