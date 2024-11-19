package org.twins.core.dao.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryTypeConfigTwinClassFieldRepository extends CrudRepository<HistoryTypeConfigTwinClassFieldEntity, UUID>, JpaSpecificationExecutor<HistoryTypeConfigTwinClassFieldEntity> {
    @Cacheable(value = "HistoryTypeConfigTwinClassFieldRepository.findConfig", key = "{#historyTypeId, #twinClassFieldId}")
    @Query(value = "from HistoryTypeConfigTwinClassFieldEntity config where config.historyType = :historyTypeId " +
            "and config.twinClassFieldId = :domainId")
    HistoryTypeConfigDomainEntity findConfig(@Param("historyTypeId") HistoryType historyTypeId, @Param("twinClassFieldId") UUID twinClassFieldId);
}
