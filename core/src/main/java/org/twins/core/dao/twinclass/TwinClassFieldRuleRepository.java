package org.twins.core.dao.twinclass;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TwinClassFieldRuleRepository extends CrudRepository<TwinClassFieldRuleEntity, UUID>, JpaSpecificationExecutor<TwinClassFieldRuleEntity> {
}
