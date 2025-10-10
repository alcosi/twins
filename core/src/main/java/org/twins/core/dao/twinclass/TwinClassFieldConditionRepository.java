package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
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
            delete from twin_class_field_condition c
            using twin_class_field_rule r,
                 twin_class_field f
            where c.twin_class_field_rule_id = r.id
              and r.twin_class_field_id = f.id
              and f.twin_class_id = :twinClassId
            """, nativeQuery = true)
    void deleteByTwinClassId(UUID twinClassId);
}
