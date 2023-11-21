package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionValidatorRepository extends CrudRepository<TwinflowTransitionValidatorEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionValidatorEntity> {
    List<TwinflowTransitionValidatorEntity> findByTwinflowTransitionIdOrderByOrder(UUID twinflowTransitionId);
}
