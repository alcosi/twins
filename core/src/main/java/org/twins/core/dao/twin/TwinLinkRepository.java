package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    <T> T findBySrcTwinIdAndDstTwinIdAndLinkId(UUID srcTwinId, UUID dstTwinId, UUID linkId, Class<T> type);
}
