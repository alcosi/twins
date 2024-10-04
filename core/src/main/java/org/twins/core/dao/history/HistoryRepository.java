package org.twins.core.dao.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryRepository extends CrudRepository<HistoryEntity, UUID>, JpaSpecificationExecutor<HistoryEntity> {
    Page<HistoryEntity> findByTwinId(UUID twinId, Pageable pageable);

    @Query(value = "select he from HistoryEntity he left join TwinEntity te on he.twinId = te.id where he.twinId = :twinId or te.headTwinId = :twinId")
    Page<HistoryEntity> findByTwinIdIncludeFirstLevelChildren(@Param("twinId") UUID twinId, Pageable pageable);
}
