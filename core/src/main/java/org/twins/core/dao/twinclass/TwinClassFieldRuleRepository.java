package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRuleRepository extends CrudRepository<TwinClassFieldRuleEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldRuleEntity> {

    /**
     * Deletes all rules whose dependent field belongs to the Twin-Class provided.
     * The query walks through <code>twin_class_field</code> table to find every field of the class
     * and removes corresponding rules in a single statement.
     */
    @Modifying
    @Query(value = """
            delete from twin_class_field_rule r
            using twin_class_field f
            where r.twin_class_field_id = f.id
              and f.twin_class_id = :twinClassId
            """, nativeQuery = true)
    void deleteByTwinClassId(UUID twinClassId);

    /**
     * Fetches all rules together with their eager-loaded conditions where the dependent field belongs to the
     * provided Twin-Class.
     */
    @Query("""
            select r
            from TwinClassFieldRuleEntity r
            join TwinClassFieldEntity f on r.twinClassFieldId = f.id
            where f.twinClassId = :twinClassId
            order by coalesce(r.rulePriority, 0) asc, r.id
            """)
    List<TwinClassFieldRuleEntity> findByTwinClassId(UUID twinClassId);

    /**
     * Fetches all rules (with eager-loaded conditions) for the specified Twin-Class field.
     */

    List<TwinClassFieldRuleEntity> findByTwinClassFieldId(UUID twinClassFieldId);
}
