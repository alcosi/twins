package org.twins.core.dao.history;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends CrudRepository<HistoryEntity, UUID>, JpaSpecificationExecutor<HistoryEntity> {
    List<HistoryEntity> findByTwinId(UUID twinId, Sort by);

    @Query(value = "select he from HistoryEntity he where he.twinId = :twinId " +
            "or he.twinId in (select child.id from TwinEntity child where child.headTwinId = :twinId)")
    List<HistoryEntity> findByTwinIdIncludeFirstLevelChildren(@Param("twinId") UUID twinId, Sort by);
}
