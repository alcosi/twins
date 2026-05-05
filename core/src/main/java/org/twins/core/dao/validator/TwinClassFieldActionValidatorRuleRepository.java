package org.twins.core.dao.validator;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinClassFieldActionValidatorRuleRepository extends CrudRepository<TwinClassFieldActionValidatorRuleEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldActionValidatorRuleEntity> {
    List<TwinClassFieldActionValidatorRuleEntity> findByTwinClassFieldIdInOrderByOrder(Set<UUID> twinClassFieldIds);
}
