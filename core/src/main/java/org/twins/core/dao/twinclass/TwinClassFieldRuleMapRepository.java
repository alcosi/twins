package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface TwinClassFieldRuleMapRepository extends CrudRepository<TwinClassFieldRuleMapEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldRuleMapEntity> {
    @Modifying
    @Query(value = """
        DELETE FROM twin_class_field_rule_map m
        WHERE m.twin_class_field_id IN (
            SELECT f.id FROM twin_class_field f 
            WHERE f.twin_class_id = :twinClassId
        )
        """, nativeQuery = true)
    void deleteByTwinClassId(@Param("twinClassId") UUID twinClassId);
}
