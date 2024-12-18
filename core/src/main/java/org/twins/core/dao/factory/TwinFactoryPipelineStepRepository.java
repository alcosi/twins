package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryPipelineStepRepository extends CrudRepository<TwinFactoryPipelineStepEntity, UUID>, JpaSpecificationExecutor<TwinFactoryPipelineStepEntity> {
    List<TwinFactoryPipelineStepEntity> findByTwinFactoryPipelineIdAndActiveTrueOrderByOrder(UUID twinFactoryPipelineId);

    @Query("SELECT t.twinFactoryConditionSetId, COUNT(t) FROM TwinFactoryPipelineStepEntity t WHERE t.twinFactoryConditionSetId IN :ids GROUP BY t.twinFactoryConditionSetId")
    List<Object[]> countByConditionSetIds(Collection<UUID> ids);
}
