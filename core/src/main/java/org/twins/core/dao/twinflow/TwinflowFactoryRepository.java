package org.twins.core.dao.twinflow;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.twins.core.domain.factory.FactoryLauncher;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowFactoryRepository extends CrudRepository<TwinflowFactoryEntity, UUID>, JpaSpecificationExecutor<TwinflowFactoryEntity> {

    List<TwinflowFactoryEntity> findByTwinflowId(UUID twinflowId);
    List<TwinflowFactoryEntity> findByTwinflowIdIn(Collection<UUID> twinflowId);
    boolean existsByTwinflowIdAndTwinFactorylauncher(UUID twinflowId, FactoryLauncher launcher);

    @Query("SELECT t.twinFactoryId, COUNT(t) FROM TwinflowFactoryEntity t WHERE t.twinFactoryId IN :ids AND t.twinFactorylauncher='afterTransitionPerform' GROUP BY t.twinFactoryId")
    List<Object[]> countByAfterTransitionPerformFactoryIds(Collection<UUID> ids);
}
