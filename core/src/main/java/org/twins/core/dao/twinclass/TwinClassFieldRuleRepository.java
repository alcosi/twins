package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassFieldRuleRepository extends CrudRepository<TwinClassFieldRuleEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldRuleEntity> {
    @Query(value = """
    SELECT DISTINCT m.twin_class_field_rule_id
    FROM twin_class_field_rule_map m
    JOIN twin_class_field f ON m.twin_class_field_id = f.id
    WHERE f.twin_class_id = :twinClassId
    """, nativeQuery = true)
    Set<UUID> findRuleIdsByTwinClassId(@Param("twinClassId") UUID twinClassId);
}
