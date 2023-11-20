package org.twins.core.dao.twinclass;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * This table is used only for speed up loading inheritance
 */
@Repository
public interface TwinClassChildMapRepository extends CrudRepository<TwinClassChildMapEntity, TwinClassChildMapEntity.PK>, JpaSpecificationExecutor<TwinClassChildMapEntity> {
    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE twin_class_child_map", nativeQuery = true)
    void truncateTable();
    List<TwinClassChildMapEntity> findAllByTwinClassId(UUID twinClassId);
}
