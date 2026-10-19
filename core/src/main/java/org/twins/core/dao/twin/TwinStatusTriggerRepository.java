package org.twins.core.dao.twin;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinStatusTriggerRepository extends CrudRepository<TwinStatusTriggerEntity, UUID>, JpaSpecificationExecutor<TwinStatusTriggerEntity> {

    String CACHE_TWIN_STATUS_TRIGGER = "TwinStatusTriggerRepository.findAllByTwinStatusIdInAndActiveTrueOrderByOrder";

    @Cacheable(value = CACHE_TWIN_STATUS_TRIGGER)
    @Query("""
        SELECT t FROM TwinStatusTriggerEntity t
        WHERE t.twinStatusId IN :twinStatusIds
        AND t.active = true
        ORDER BY t.order
        """)
    List<TwinStatusTriggerEntity> findAllByTwinStatusIdInAndActiveTrueOrderByOrder(@Param("twinStatusIds") List<UUID> twinStatusIds);
}
