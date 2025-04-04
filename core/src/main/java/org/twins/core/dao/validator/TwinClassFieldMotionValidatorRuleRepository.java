package org.twins.core.dao.validator;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldMotionValidatorRuleRepository extends CrudRepository<TwinClassFieldMotionValidatorRuleEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldMotionValidatorRuleEntity> {
    List<TwinClassFieldMotionValidatorRuleEntity> findAllByFieldMotionIdInOrderByOrder(Collection<UUID> fieldMotionIds);
}
