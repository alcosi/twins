package org.twins.core.dao.history;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryTypeConfigTwinClassChildRepository extends CrudRepository<HistoryTypeConfigTwinClassChildEntity, UUID>, JpaSpecificationExecutor<HistoryTypeConfigTwinClassChildEntity> {

    @Cacheable(value = "HistoryTypeConfigTwinClassChildRepository.findConfig", key = "{#childHistoryType, #childTwinClassId}")
    @Query(value = "from HistoryTypeConfigTwinClassChildEntity config where config.childHistoryType = :childHistoryType " +
            "and config.childTwinClassId = :childTwinClassId")
    List<HistoryTypeConfigTwinClassChildEntity> findConfig(@Param("childHistoryType") HistoryType childHistoryType, @Param("childTwinClassId") UUID childTwinClassId);
}
