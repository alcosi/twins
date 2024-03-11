package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowRepository extends CrudRepository<TwinflowEntity, UUID>, JpaSpecificationExecutor<TwinflowEntity> {
    List<TwinflowEntity> findByTwinClassId(UUID twinClassId);

    @Query(value = "from TwinflowEntity where id = function('twinflowDetect', :domainId, :businessAccountId, :twinflowSpaceId, :twinClassId)")
    TwinflowEntity twinflowDetect(
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") UUID businessAccountId,
            @Param("twinflowSpaceId") UUID twinflowSpaceId,
            @Param("twinClassId") UUID twinClassId
    );

    @Query(value = "select distinct concat(coalesce(cast(te.twinClassId as string), ''), coalesce(cast(te.twinflowSchemaSpaceId as string), '')) as key, tw from TwinEntity te, TwinflowEntity tw " +
            " where te.id in (:twinIds) and tw.id = function('twinflowDetect', :domainId, :businessAccountId, te.twinflowSchemaSpaceId, te.twinClassId)")
    List<Object[]> twinflowsDetect(
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") UUID businessAccountId,
            @Param("twinIds") Collection<UUID> twinIds
    );
    List<TwinflowEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds);
}
