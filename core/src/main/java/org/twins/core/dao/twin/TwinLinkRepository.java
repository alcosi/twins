package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinLinkRepository extends CrudRepository<TwinLinkEntity, UUID>, JpaSpecificationExecutor<TwinLinkEntity> {
    List<TwinLinkEntity> findBySrcTwinIdOrDstTwinId(UUID srcTwinId, UUID dstTwinId);

    @Modifying
    @Query(value = "from TwinLinkEntity twinLink " +
            "where twinLink.id in (:linkIdList) and (twinLink.srcTwinId = :twinId or twinLink.dstTwinId = :twinId)")
    void deleteNotMandatory(@Param("twinId") UUID twinId, @Param("linkIdList") List<UUID> twinLinksDeleteUUIDList);
}
