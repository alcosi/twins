package org.twins.core.dao.validator;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinActionValidatorRuleRepository extends CrudRepository<TwinActionValidatorRuleEntity, UUID>, JpaSpecificationExecutor<TwinActionValidatorRuleEntity> {
    List<TwinActionValidatorRuleEntity> findByTwinClassIdOrderByOrder(UUID twinClassId);

    List<TwinActionValidatorRuleEntity> findByTwinClassIdIn(Set<UUID> twinClassIds);

    List<TwinActionValidatorRuleEntity> findByTwinClassIdInOrderByOrder(Set<UUID> twinClassIds);
}
