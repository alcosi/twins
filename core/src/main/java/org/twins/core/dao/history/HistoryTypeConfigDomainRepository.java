package org.twins.core.dao.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryTypeConfigDomainRepository extends CrudRepository<HistoryTypeConfigDomainEntity, UUID>, JpaSpecificationExecutor<HistoryTypeConfigDomainEntity> {
    @Cacheable(value = "HistoryTypeConfigDomainRepository.findConfig", key = "{#historyTypeId, #domainId}")
    @Query(value = "from HistoryTypeConfigDomainEntity config where config.historyType = :historyTypeId " +
            "and config.domainId = :domainId")
    HistoryTypeConfigDomainEntity findConfig(@Param("historyTypeId") HistoryType historyTypeId, @Param("domainId") UUID domainId);
}
