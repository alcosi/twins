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
public interface TwinClassFieldConditionRepository extends CrudRepository<TwinClassFieldConditionEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldConditionEntity> {
    List<TwinClassFieldConditionEntity> findByTwinClassFieldRuleIdIn(Set<UUID> twinClassFieldRuleIdList);

    /**
     * Deletes all rule-conditions whose owning rule belongs to fields of the provided Twin-Class.
     * Implementation uses a join against <code>twin_class_field_rule</code> and <code>twin_class_field</code>
     * so that everything is purged in a single DB round-trip.
     */
    @Modifying
    @Query(value = """
        DELETE FROM twin_class_field_condition c
        WHERE c.twin_class_field_rule_id IN (
            SELECT DISTINCT m.twin_class_field_rule_id
            FROM twin_class_field_rule_map m
            JOIN twin_class_field f ON m.twin_class_field_id = f.id
            WHERE f.twin_class_id = :twinClassId
        )
        """, nativeQuery = true)
    void deleteByTwinClassId(@Param("twinClassId") UUID twinClassId);
}
