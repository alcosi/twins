package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Repository
public interface TwinflowTransitionTriggerRepository extends CrudRepository<TwinflowTransitionTriggerEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionTriggerEntity> {
}
