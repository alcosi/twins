package org.twins.core.dao.twinclassfieldrecompute;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinClassFieldRecomputeOnActionValidatorRuleRepository
        extends CrudRepository<TwinClassFieldRecomputeOnActionValidatorRuleEntity, UUID>,
                JpaSpecificationExecutor<TwinClassFieldRecomputeOnActionValidatorRuleEntity> {

    List<TwinClassFieldRecomputeOnActionValidatorRuleEntity> findByTwinClassFieldRecomputeOnActionIdOrderByOrder(
            UUID twinClassFieldRecomputeOnActionId);

    List<TwinClassFieldRecomputeOnActionValidatorRuleEntity> findByTwinClassFieldRecomputeOnActionIdInOrderByOrder(
            Collection<UUID> twinClassFieldRecomputeOnActionIds);
}
