package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryMultiplierRepository extends CrudRepository<TwinFactoryMultiplierEntity, UUID>, JpaSpecificationExecutor<TwinFactoryMultiplierEntity> {
    List<TwinFactoryMultiplierEntity> findByTwinFactoryId(UUID twinFactoryId);

    @Query("SELECT t.twinFactoryId, COUNT(t) FROM TwinFactoryMultiplierEntity t WHERE t.twinFactoryId IN :ids GROUP BY t.twinFactoryId")
    List<Object[]> countByTwinFactoryIds(Collection<UUID> ids);
}
