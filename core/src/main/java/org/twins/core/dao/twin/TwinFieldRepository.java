package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFieldRepository extends CrudRepository<TwinFieldEntity, UUID>, JpaSpecificationExecutor<TwinFieldEntity> {

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    @Query(value = "select count(child) from TwinEntity child where child.headTwinId=:headTwinId and not child.twinStatusId in :childrenTwinStatusIdList")
    long countChildrenTwinsWithStatusNotIn(@Param("headTwinId") UUID headTwinId, @Param("childrenTwinStatusIdList") Collection<UUID> childrenTwinStatusIdList);

    List<TwinFieldEntity> findByTwinId(UUID twinId);

    List<TwinFieldEntity> findByTwinIdIn(Collection<UUID> twinIdList);

    TwinFieldEntity findByTwinIdAndTwinClassField_Key(UUID twinId, String key);

    TwinFieldEntity findByTwinIdAndTwinClassFieldId(UUID twinId, UUID twinClassFieldId);

    void deleteByTwinId(UUID twinId);
}
