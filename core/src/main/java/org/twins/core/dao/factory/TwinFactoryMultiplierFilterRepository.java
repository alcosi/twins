package org.twins.core.dao.factory;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinFactoryMultiplierFilterRepository extends CrudRepository<TwinFactoryMultiplierFilterEntity, UUID>, JpaSpecificationExecutor<TwinFactoryMultiplierFilterEntity> {
    List<TwinFactoryMultiplierFilterEntity> findByTwinFactoryMultiplierId(UUID twinFactoryMultiplierId);

    @Query("SELECT t.twinFactoryConditionSetId, COUNT(t) FROM TwinFactoryMultiplierFilterEntity t WHERE t.twinFactoryConditionSetId IN :ids GROUP BY t.twinFactoryConditionSetId")
    List<Object[]> countByConditionSetIds(Collection<UUID> ids);
}
