package org.twins.core.dao.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryTypeRepository extends CrudRepository<HistoryTypeEntity, UUID>, JpaSpecificationExecutor<HistoryTypeEntity> {
    @Cacheable(value = "HistoryTypeRepository.findConfig", key = "{#historyTypeId}")
    @Query(value = "from HistoryTypeEntity config where config.id = :historyTypeId ")
    HistoryTypeEntity findConfig(@Param("historyTypeId") HistoryType historyTypeId);
}
