package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryPipelineRepository extends CrudRepository<TwinFactoryPipelineEntity, UUID>, JpaSpecificationExecutor<TwinFactoryPipelineEntity> {
    List<TwinFactoryPipelineEntity> findByTwinFactoryIdAndActiveTrue(UUID twinFactoryId);

    @Query("SELECT p.nextTwinFactoryId, COUNT(p) FROM TwinFactoryPipelineEntity p WHERE p.nextTwinFactoryId IN :ids GROUP BY p.nextTwinFactoryId")
    List<Object[]> countByNextTwinFactoryIds(Collection<UUID> ids);

    @Query("SELECT t.twinFactoryId, COUNT(t) FROM TwinFactoryPipelineEntity t WHERE t.twinFactoryId IN :ids GROUP BY t.twinFactoryId")
    List<Object[]> countByTwinFactoryIds(Collection<UUID> ids);

    @Query("SELECT t.twinFactoryConditionSetId, COUNT(t) FROM TwinFactoryPipelineEntity t WHERE t.twinFactoryConditionSetId IN :ids GROUP BY t.twinFactoryConditionSetId")
    List<Object[]> countByConditionSetIds(Collection<UUID> ids);

    @Query("SELECT p.afterCommitTwinFactoryId, COUNT(p) FROM TwinFactoryPipelineEntity p WHERE p.afterCommitTwinFactoryId IN :ids GROUP BY p.afterCommitTwinFactoryId")
    List<Object[]> countByAfterCommitTwinFactoryIds(Collection<UUID> ids);
}
