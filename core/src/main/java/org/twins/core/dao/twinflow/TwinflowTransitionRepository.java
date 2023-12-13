package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionRepository extends CrudRepository<TwinflowTransitionEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionEntity> {
    List<TwinflowTransitionEntity> findByTwinflowIdAndSrcTwinStatusId(UUID twinflowId, UUID srcTwinStatusID);
    List<TwinflowTransitionEntity> findByTwinflowTransitionAliasId(String alias); //todo also filter by current domainId
}
