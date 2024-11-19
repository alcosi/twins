package org.twins.core.dao.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryTypeConfigTwinClassRepository extends CrudRepository<HistoryTypeConfigTwinClassEntity, UUID>, JpaSpecificationExecutor<HistoryTypeConfigTwinClassEntity> {

    @Cacheable(value = "HistoryTypeConfigTwinClassRepository.findConfig", key = "{#historyTypeId, #twinClassId}")
    @Query(value = "from HistoryTypeConfigTwinClassEntity config where config.historyType = :historyTypeId " +
            "and config.twinClassId = :twinClassId")
    HistoryTypeConfigTwinClassEntity findConfig(@Param("historyTypeId") HistoryType historyTypeId, @Param("twinClassId") UUID twinClassId);
}
